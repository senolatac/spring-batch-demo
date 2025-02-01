package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.ArilBatchConfig;
import com.aril.arilbatchsdk.TestConfig;
import com.aril.arilbatchsdk.config.job.*;
import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.explore.ArilJobExplorer;
import com.aril.arilbatchsdk.core.item.controller.ControllerItemReader;
import com.aril.arilbatchsdk.core.item.controller.ControllerItemReaderBuilder;
import com.aril.arilbatchsdk.core.item.service.reader.ServiceItemReader;
import com.aril.arilbatchsdk.core.item.service.reader.ServiceItemReaderBuilder;
import com.aril.arilbatchsdk.core.parameter.*;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryIdempotentItemWriterParameter;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryItemReaderParameter;
import com.aril.arilbatchsdk.core.parameter.support.ServiceIdempotentItemWriterParameter;
import com.aril.arilbatchsdk.entity.BatchJobConsumerTracker;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.facade.BatchConsumerTrackerFacade;
import com.aril.arilbatchsdk.testadapter.controller.BookController;
import com.aril.arilbatchsdk.testadapter.entity.BookDetailEntity;
import com.aril.arilbatchsdk.testadapter.entity.BookEntity;
import com.aril.arilbatchsdk.testadapter.repository.BookRepository;
import com.aril.arilbatchsdk.testadapter.service.BookReaderService;
import com.aril.arilbatchsdk.testadapter.service.BookWriterService;
import com.aril.arilbatchsdk.testadapter.service.JobExecutionListenerService;
import com.aril.arilbatchsdk.util.MethodUtils;
import com.aril.arilkafka.support.utils.ConsumerConfigUtils;
import com.aril.valhala.batch.BatchItem;
import com.aril.valhala.product.ModuleType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest(classes = ArilBatchConfig.class)
@SpringBatchTest
@Import(TestConfig.class)
public abstract class BaseJobLauncherTest {

    @Autowired
    protected ArilJobExplorer arilJobExplorer;

    @Autowired
    protected BatchJobSummaryService arilBatchJobSummaryService;

    @Autowired
    protected BatchConsumerTrackerService arilBatchConsumerTrackerService;

    @Autowired
    protected BatchConsumerTrackerFacade batchConsumerTrackerFacade;

    @Autowired
    protected JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    protected BookRepository bookRepository;

    @Autowired
    protected BookReaderService bookReaderService;

    @Autowired
    protected BookController bookController;

    @Autowired
    protected BookWriterService bookWriterService;

    @SpyBean
    @Autowired
    protected JobExecutionListenerService jobExecutionListenerService;

    @Value("classpath:data/book-test-data.csv")
    private Resource bookTestDataCsv;

    @Value("classpath:data/book-test-data.xlsx")
    private Resource bookTestDataExcel;

    protected static final String JOB_OWNER = "job-owner";
    protected static final String TOPIC = "test-topic";
    protected static final String BUCKET = "bucket";
    protected static final ModuleType ACTIVE_MODULE = ModuleType.THOR_HOST;

    @BeforeEach
    void setUp() {
        prepareBooks();
    }

    @AfterEach
    void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
        bookRepository.deleteAll();
    }

    protected void assertSyncCall(JobExecution jobExecution, String outputId, boolean exporter, boolean consumer) throws Exception {
        log.info("Job started with id: {} and paramId: {}", jobExecution.getJobId(), outputId);

        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertExecutionResult(jobExecution.getJobId(), exporter, consumer);
    }

    protected void assertAsyncCall(JobExecution jobExecution, String outputId, boolean exporter, boolean consumer) throws Exception {
        log.info("Job started with id: {} and paramId: {}", jobExecution.getJobId(), outputId);

        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.UNKNOWN);
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .until(() -> jobExecution.getExitStatus().equals(ExitStatus.COMPLETED));
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertExecutionResult(jobExecution.getJobId(), exporter, consumer);
        verify(jobExecutionListenerService).statusChanged(jobExecution);
    }

    protected void assertExecutionResult(Long jobId, boolean exporter, boolean consumer) throws Exception {
        BatchJobSummary jobSummary = arilBatchJobSummaryService.getWithDetails(jobId);
        List<JobExecution> jobExecutions = arilJobExplorer.getJobExecutions(jobId);

        assertThat(jobSummary.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobSummary.getBatchType()).isNotNull();
        assertThat(jobSummary.getEndTime()).isNotNull();
        assertThat(jobSummary.getModule()).isEqualTo(ACTIVE_MODULE);

        for (JobExecution jobExecution : jobExecutions) {
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
                log.info("StepExecution details with name: {} read: {} write: {} skip: {}", stepExecution.getStepName(), stepExecution.getReadCount(), stepExecution.getWriteCount(), stepExecution.getSkipCount());
            }
        }
        if (exporter) {
            assertThat(jobSummary.getExportUrl()).isNotBlank();
            log.info("Job export-url is {}", jobSummary.getExportUrl());
        }
        if (consumer) {
            assertConsumerTrackerState(jobId);
        }
    }

    protected void assertConsumerTrackerState(Long jobId) throws Exception {
        batchConsumerTrackerFacade.updateStatusOfConsumerTracker(jobId, ConsumerConfigUtils.DEFAULT_CONSUMER_GROUP);

        BatchJobConsumerTracker consumerTracker = arilBatchConsumerTrackerService.getJobConsumerTracker(jobId);

        assertThat(consumerTracker.getProduceSnapshot()).isNotEmpty();
        log.info("consumer details read: {} skip: {} produce: {} percentage: {}", consumerTracker.getReadCount(), consumerTracker.getProduceSkipCount(), consumerTracker.getProduceCount(), consumerTracker.getConsumePercentage());
    }

    protected void prepareBooks() {
        for (int i = 0; i < 100; i++) {
            BookDetailEntity detailEntity = BookDetailEntity.builder()
                    .detail("detail " + i)
                    .build();

            BookEntity bookEntity = BookEntity.builder()
                    .author("author " + i)
                    .title("title " + i)
                    .year(i)
                    .bookDetail(detailEntity)
                    .build();

            bookRepository.save(bookEntity);
        }
    }

    protected BatchExcelToDbJobParameter inputParamForExcelToDbJob() throws IOException {
        return BatchExcelToDbJobParameter.builder()
                .chunkSize(3)
                .jobOwner(JOB_OWNER)
                .inputFields(new String[]{"title", "author", "year"})
                .jobName(ArilBatchFromExcelToDbConfig.READER_NAME)
                .repositoryItemWriter(RepositoryIdempotentItemWriterParameter.builder()
                        .methodName("save")
                        .repository(bookRepository)
                        .build())
                .itemInputClass(BookEntity.class)
                .inputFileStream(bookTestDataExcel.getInputStream())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchExcelToKafkaJobParameter inputParamForExcelToKafkaJob() throws IOException {
        return BatchExcelToKafkaJobParameter.builder()
                .chunkSize(3)
                .topic(TOPIC)
                .jobOwner(JOB_OWNER)
                .inputFields(new String[]{"title", "author", "year"})
                .jobName(ArilBatchFromExcelToKafkaConfig.READER_NAME)
                .itemInputClass(BookEntity.class)
                .inputFilePath(bookTestDataExcel.getFile().getAbsolutePath())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchExcelToServiceJobParameter inputParamForExcelToServiceJob() throws IOException {
        return BatchExcelToServiceJobParameter.builder()
                .chunkSize(3)
                .jobOwner(JOB_OWNER)
                .inputFields(new String[]{"title", "author", "year"})
                .jobName(ArilBatchFromExcelToServiceConfig.READER_NAME)
                .serviceItemWriter(serviceItemWriter())
                .itemInputClass(BookEntity.class)
                .inputFilePath(bookTestDataExcel.getFile().getAbsolutePath())
                .inputFileStream(bookTestDataExcel.getInputStream())
                .idempotencyOptions(IdempotencyOptions.builder().enable(false).build())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchCsvToDbJobParameter inputParamForCsvToDbJob() throws IOException {
        return BatchCsvToDbJobParameter.builder()
                .chunkSize(3)
                .jobOwner(JOB_OWNER)
                .inputFields(new String[]{"title", "author", "year"})
                .jobName(ArilBatchFromCsvToDbConfig.READER_NAME)
                .repositoryItemWriter(RepositoryIdempotentItemWriterParameter.builder()
                        .methodName("save")
                        .repository(bookRepository)
                        .build())
                .itemInputClass(BookEntity.class)
                .inputFilePath(bookTestDataCsv.getFile().getAbsolutePath())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchCsvToServiceJobParameter inputParamForCsvToServiceJob() throws IOException {
        return BatchCsvToServiceJobParameter.builder()
                .chunkSize(3)
                .jobOwner(JOB_OWNER)
                .inputFields(new String[]{"title", "author", "year"})
                .jobName(ArilBatchFromCsvToServiceConfig.READER_NAME)
                .serviceItemWriter(serviceItemWriter())
                .itemInputClass(BookEntity.class)
                .inputFilePath(bookTestDataCsv.getFile().getAbsolutePath())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchCsvToKafkaJobParameter inputParamForCsvToKafkaJob() throws IOException {
        return BatchCsvToKafkaJobParameter.builder()
                .chunkSize(3)
                .inputFields(new String[]{"title", "author", "year"})
                .jobName(ArilBatchFromCsvToKafkaConfig.READER_NAME)
                .itemInputClass(BookEntity.class)
                .inputFilePath(bookTestDataCsv.getFile().getAbsolutePath())
                .topic(TOPIC)
                .jobOwner(JOB_OWNER)
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchDbToCsvJobParameter inputParamForDbToCsvJob() {
        return BatchDbToCsvJobParameter.builder()
                .jobName(ArilBatchFromDbToCsvConfig.READER_NAME)
                .chunkSize(3)
                .exportBucket(BUCKET)
                .jobOwner(JOB_OWNER)
                .outputFields(new String[]{"id", "title", "author", "year"})
                .repositoryItemReader(repositoryItemReader())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchDbToExcelJobParameter inputParamForDbToExcelJob() {
        return BatchDbToExcelJobParameter.builder()
                .jobName(ArilBatchFromDbToExcelConfig.READER_NAME)
                .chunkSize(3)
                .exportBucket(BUCKET)
                .jobOwner(JOB_OWNER)
                .outputFields(new String[]{"id", "title", "author", "year"})
                .headerNames(new String[]{"Id", "Title", "Author", "Year"})
                .repositoryItemReader(repositoryItemReader())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchDbToServiceJobParameter inputParamForDbToServiceJob() {
        return BatchDbToServiceJobParameter.builder()
                .jobName(ArilBatchFromDbToServiceConfig.READER_NAME)
                .chunkSize(3)
                .jobOwner(JOB_OWNER)
                .repositoryItemReader(repositoryItemReader())
                .serviceItemWriter(serviceItemWriter())
                .idempotencyOptions(IdempotencyOptions.builder().enable(false).build())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchServiceToExcelJobParameter inputParamForServiceToExcelJob() {
        return inputParamForServiceToExcelJob(null);
    }

    protected BatchServiceToExcelJobParameter inputParamForServiceToExcelJob(ItemProcessor<? extends BatchItem, ? extends BatchItem> itemProcessor) {
        return BatchServiceToExcelJobParameter.builder()
                .jobName(ArilBatchFromServiceToExcelConfig.READER_NAME)
                .chunkSize(3)
                .exportBucket(BUCKET)
                .jobOwner(JOB_OWNER)
                .outputFields(new String[]{"id", "title", "author", "year", "bookDetail.detail"})
                .serviceItemReader(serviceItemReader())
                .itemProcessor(itemProcessor)
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchServiceToDbJobParameter inputParamForServiceToDbJob() {
        return BatchServiceToDbJobParameter.builder()
                .jobName(ArilBatchFromServiceToDbConfig.READER_NAME)
                .chunkSize(3)
                .jobOwner(JOB_OWNER)
                .serviceItemReader(serviceItemReader())
                .repositoryItemWriter(RepositoryIdempotentItemWriterParameter.builder()
                        .methodName("save")
                        .repository(bookRepository)
                        .build())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchControllerToExcelJobParameter inputParamForControllerToExcelJob() {
        return inputParamForControllerToExcelJob(null);
    }

    protected BatchControllerToExcelJobParameter inputParamForControllerToExcelJob(ItemProcessor<? extends BatchItem, ? extends BatchItem> itemProcessor) {
        return BatchControllerToExcelJobParameter.builder()
                .jobName(ArilBatchFromControllerToExcelConfig.READER_NAME)
                .chunkSize(3)
                .exportBucket(BUCKET)
                .jobOwner(JOB_OWNER)
                .outputFields(new String[]{"id", "title", "author", "year"})
                .controllerItemReader(controllerItemReader())
                .itemProcessor(itemProcessor)
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchControllerToCsvJobParameter inputParamForControllerToCsvJob() {
        return inputParamForControllerToCsvJob(null);
    }

    protected BatchControllerToCsvJobParameter inputParamForControllerToCsvJob(ItemProcessor<? extends BatchItem, ? extends BatchItem> itemProcessor) {
        return BatchControllerToCsvJobParameter.builder()
                .jobName(ArilBatchFromControllerToCsvConfig.READER_NAME)
                .chunkSize(3)
                .exportBucket(BUCKET)
                .jobOwner(JOB_OWNER)
                .outputFields(new String[]{"id", "title", "author", "year", "bookDetail.detail"})
                .controllerItemReader(controllerItemReader())
                .itemProcessor(itemProcessor)
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchServiceToKafkaJobParameter inputParamForServiceToKafkaJob() {
        return BatchServiceToKafkaJobParameter.builder()
                .chunkSize(3)
                .jobName(ArilBatchFromServiceToKafkaConfig.READER_NAME)
                .topic(TOPIC)
                .jobOwner(JOB_OWNER)
                .serviceItemReader(serviceItemReader())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected BatchDbToKafkaJobParameter inputParamForDbToKafkaJob() {
        return BatchDbToKafkaJobParameter.builder()
                .chunkSize(3)
                .jobName(ArilBatchFromDbToKafkaConfig.READER_NAME)
                .topic(TOPIC)
                .jobOwner(JOB_OWNER)
                .repositoryItemReader(repositoryItemReader())
                .metadata(Map.of("key1", "val1", "key2", 1L))
                .jobExecutionChangeSubscriber(jobExecutionSubscriber())
                .build();
    }

    protected RepositoryItemReaderParameter repositoryItemReader() {
        return RepositoryItemReaderParameter.builder()
                .repository(bookRepository)
                .methodName("findAll")
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    protected ControllerItemReader<BookEntity> controllerItemReader() {
        return new ControllerItemReaderBuilder<BookEntity>()
                .name(ArilBatchFromControllerToExcelConfig.READER_NAME)
                .controller(bookController)
                .method(findControllerFilterMethod())
                .arguments("{\"pager\": {\"pageSize\":10, \"pageNumber\": 0}}")
                .pageSize(3)
                .build();
    }

    protected ServiceItemReader<BookEntity> serviceItemReader() {
        return new ServiceItemReaderBuilder<BookEntity>()
                .name(ArilBatchFromServiceToExcelConfig.READER_NAME)
                .service(bookReaderService)
                .methodName("findAll")
                .pageSize(3)
                .build();
    }

    protected ServiceIdempotentItemWriterParameter serviceItemWriter() {
        return ServiceIdempotentItemWriterParameter.builder()
                .service(bookWriterService)
                .methodName("executeWithParam")
                .arguments(List.of(5))
                .build();
    }

    protected Consumer<JobExecution> jobExecutionSubscriber() {
        return jobExecution -> jobExecutionListenerService.statusChanged(jobExecution);
    }

    private Method findControllerFilterMethod() {
        return MethodUtils.getMethodsAnnotatedWith(BookController.class, PostMapping.class).get(0);
    }
}
