package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.FileUploader;
import com.aril.arilbatchsdk.core.item.excel.ExcelStreamItemWriter;
import com.aril.arilbatchsdk.core.item.service.reader.ServiceItemReader;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchServiceToExcelJobParameter;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;

@Lazy
@Configuration
public class ArilBatchFromServiceToExcelConfig extends AbstractArilBatchConfig {

    public static final String READER_NAME = "readerForServiceToExcelJob";
    private static final String WRITER_NAME = "writerForServiceToExcelJob";
    private static final String CHUNK_STEP_NAME = "chunkStepForServiceToExcelJob";
    private static final String LOG_STEP_NAME = "logStepForServiceToExcelJob";
    private static final String FILE_UPLOADER_STEP_NAME = "fileUploaderForServiceToExcelJob";
    private static final String TMP_FILE_DELETE_STEP_NAME = "tmpFileDeleteForServiceToExcelJob";
    private static final String JOB_NAME = "jobForServiceToExcelJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_SERVICE_TO_EXCEL)
    public Job jobForServiceToExcelJob(JobRepository jobRepository,
                                       Step chunkStepForServiceToExcelJob,
                                       Step uploaderStepForServiceToExcelJob,
                                       Step tmpFileDeleteStepForServiceToExcelJob,
                                       Step logStepForServiceToExcelJob,
                                       JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForServiceToExcelJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForServiceToExcelJob)
                .next(uploaderStepForServiceToExcelJob)
                .next(tmpFileDeleteStepForServiceToExcelJob)
                .next(logStepForServiceToExcelJob)
                .listener(jobExecutionStatusChangeListenerForServiceToExcelJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForServiceToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToExcelJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends BatchItem> Step chunkStepForServiceToExcelJob(JobRepository jobRepository,
                                                                    ArilBatchProperties arilBatchProperties,
                                                                    PlatformTransactionManager transactionManager,
                                                                    BatchJobSummaryService arilBatchJobSummaryService,
                                                                    @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToExcelJobParameter> inputParam,
                                                                    @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<BatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForServiceToExcelJob(inputParam))
                .processor((ItemProcessor<? super BatchItem, ? extends D>) processorForServiceToExcelJob(inputParam))
                .writer(writerForServiceToExcelJob(inputParam, identifier))
                .listener(new ChunkStepExecutionListener(BatchType.SERVICE_TO_EXCEL, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .readerIsTransactionalQueue()//to create a reader that does not buffer items in Java
                .build();
    }

    @Bean
    @JobScope
    public Step uploaderStepForServiceToExcelJob(JobRepository jobRepository,
                                                 FileUploader fileUploader,
                                                 PlatformTransactionManager transactionManager,
                                                 @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToExcelJobParameter> inputParam,
                                                 @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                                 @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(FILE_UPLOADER_STEP_NAME, jobRepository)
                .tasklet(new ExcelFileUploaderTasklet(fileUploader, inputParam.getJobParameter().getExportBucket(), exportFolder, JobUtils.getExcelFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step tmpFileDeleteStepForServiceToExcelJob(JobRepository jobRepository,
                                                      PlatformTransactionManager transactionManager,
                                                      @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(TMP_FILE_DELETE_STEP_NAME, jobRepository)
                .tasklet(new LocalFileDeleteTasklet(JobUtils.getExcelFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step logStepForServiceToExcelJob(JobRepository jobRepository,
                                            FileUploader fileUploader,
                                            BatchJobSummaryService arilBatchJobSummaryService,
                                            PlatformTransactionManager transactionManager,
                                            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToExcelJobParameter> inputParam,
                                            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(LOG_STEP_NAME, jobRepository)
                .tasklet(new ExportMetadataTasklet(fileUploader, arilBatchJobSummaryService, exportFolder, JobUtils.getExcelFilenameById(identifier), inputParam.getJobParameter().getExportBucket()), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends BatchItem> ServiceItemReader<T> readerForServiceToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToExcelJobParameter> inputParam) {
        return (ServiceItemReader<T>) inputParam.getJobParameter().getServiceItemReader();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends BatchItem> ItemProcessor<T, T> processorForServiceToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToExcelJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends BatchItem> ExcelStreamItemWriter<D> writerForServiceToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToExcelJobParameter> inputParam,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return configureExcelStreamItemWriter(
                WRITER_NAME,
                identifier,
                inputParam.getJobParameter().getOutputFields(),
                inputParam.getJobParameter().getHeaderColumns(),
                inputParam.getJobParameter().getThousandSeparatorFormat()
        );
    }
}
