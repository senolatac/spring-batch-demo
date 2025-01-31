package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.FileUploader;
import com.aril.arilbatchsdk.core.item.controller.ControllerItemReader;
import com.aril.arilbatchsdk.core.item.excel.ExcelStreamItemWriter;
import com.aril.arilbatchsdk.core.item.excel.ExcelStreamItemWriterBuilder;
import com.aril.arilbatchsdk.core.item.excel.support.DefaultExcelStreamHeaderCallback;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchControllerToExcelJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.core.tasklet.export.ExportMetadataTasklet;
import com.aril.arilbatchsdk.core.tasklet.file.ExcelFileUploaderTasklet;
import com.aril.arilbatchsdk.core.tasklet.file.LocalFileDeleteTasklet;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import com.aril.arilbatchsdk.util.JobUtils;
import com.aril.valhala.batch.BatchItem;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;

@Lazy
@Configuration
@ConditionalOnClass(ResponseEntity.class)
public class ArilBatchFromControllerToExcelConfig {

    public static final String READER_NAME = "readerForControllerToExcelJob";
    private static final String WRITER_NAME = "writerForControllerToExcelJob";
    private static final String CHUNK_STEP_NAME = "chunkStepForControllerToExcelJob";
    private static final String LOG_STEP_NAME = "logStepForControllerToExcelJob";
    private static final String FILE_UPLOADER_STEP_NAME = "fileUploaderForControllerToExcelJob";
    private static final String TMP_FILE_DELETE_STEP_NAME = "tmpFileDeleteForControllerToExcelJob";
    private static final String JOB_NAME = "jobForControllerToExcelJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_CONTROLLER_TO_EXCEL)
    public Job jobForControllerToExcelJob(JobRepository jobRepository,
                                          Step chunkStepForControllerToExcelJob,
                                          Step uploaderStepForControllerToExcelJob,
                                          Step tmpFileDeleteStepForControllerToExcelJob,
                                          Step logStepForControllerToExcelJob,
                                          JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForControllerToExcelJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForControllerToExcelJob)
                .next(uploaderStepForControllerToExcelJob)
                .next(tmpFileDeleteStepForControllerToExcelJob)
                .next(logStepForControllerToExcelJob)
                .listener(jobExecutionStatusChangeListenerForControllerToExcelJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForControllerToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToExcelJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends BatchItem> Step chunkStepForControllerToExcelJob(JobRepository jobRepository,
                                                                       ArilBatchProperties arilBatchProperties,
                                                                       PlatformTransactionManager transactionManager,
                                                                       BatchJobSummaryService arilBatchJobSummaryService,
                                                                       @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToExcelJobParameter> inputParam,
                                                                       @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<BatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForControllerToExcelJob(inputParam))
                .processor((ItemProcessor<? super BatchItem, ? extends D>) processorForControllerToExcelJob(inputParam))
                .writer(writerForControllerToExcelJob(inputParam, identifier))
                .listener(new ChunkStepExecutionListener(BatchType.CONTROLLER_TO_EXCEL, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .readerIsTransactionalQueue()//to create a reader that does not buffer items in Java
                .build();
    }

    @Bean
    @JobScope
    public Step uploaderStepForControllerToExcelJob(JobRepository jobRepository,
                                                    FileUploader fileUploader,
                                                    PlatformTransactionManager transactionManager,
                                                    @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToExcelJobParameter> inputParam,
                                                    @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                                    @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(FILE_UPLOADER_STEP_NAME, jobRepository)
                .tasklet(new ExcelFileUploaderTasklet(fileUploader, inputParam.getJobParameter().getExportBucket(), exportFolder, JobUtils.getExcelFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step tmpFileDeleteStepForControllerToExcelJob(JobRepository jobRepository,
                                                         PlatformTransactionManager transactionManager,
                                                         @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(TMP_FILE_DELETE_STEP_NAME, jobRepository)
                .tasklet(new LocalFileDeleteTasklet(JobUtils.getExcelFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step logStepForControllerToExcelJob(JobRepository jobRepository,
                                               FileUploader fileUploader,
                                               BatchJobSummaryService arilBatchJobSummaryService,
                                               PlatformTransactionManager transactionManager,
                                               @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToExcelJobParameter> inputParam,
                                               @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                               @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(LOG_STEP_NAME, jobRepository)
                .tasklet(new ExportMetadataTasklet(fileUploader, arilBatchJobSummaryService, exportFolder, JobUtils.getExcelFilenameById(identifier), inputParam.getJobParameter().getExportBucket()), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends BatchItem> ControllerItemReader<T> readerForControllerToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToExcelJobParameter> inputParam) {
        return (ControllerItemReader<T>) inputParam.getJobParameter().getControllerItemReader();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends BatchItem> ItemProcessor<T, T> processorForControllerToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToExcelJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends BatchItem> ExcelStreamItemWriter<D> writerForControllerToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToExcelJobParameter> inputParam,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new ExcelStreamItemWriterBuilder<D>()
                .name(WRITER_NAME)
                .resource(new FileSystemResource(JobUtils.getExcelFilenameById(identifier)))
                .columnNames(inputParam.getJobParameter().getOutputFields())
                .headerCallback(new DefaultExcelStreamHeaderCallback(inputParam.getJobParameter().getHeaderColumns()))
                .thousandSeparatorFormat(inputParam.getJobParameter().getThousandSeparatorFormat())
                .build();
    }
}
