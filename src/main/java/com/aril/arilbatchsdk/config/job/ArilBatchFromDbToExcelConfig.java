package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.FileUploader;
import com.aril.arilbatchsdk.core.item.excel.ExcelStreamItemWriter;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchDbToExcelJobParameter;
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
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.PlatformTransactionManager;

@Lazy
@Configuration
@ConditionalOnClass(JpaRepository.class)
public class ArilBatchFromDbToExcelConfig extends AbstractArilBatchConfig {

    public static final String READER_NAME = "readerForDbToExcelJob";
    private static final String WRITER_NAME = "writerForDbToExcelJob";
    private static final String CHUNK_STEP_NAME = "chunkStepForDbToExcelJob";
    private static final String LOG_STEP_NAME = "logStepForDbToExcelJob";
    private static final String FILE_UPLOADER_STEP_NAME = "fileUploaderForDbToExcelJob";
    private static final String TMP_FILE_DELETE_STEP_NAME = "tmpFileDeleteForDbToExcelJob";
    private static final String JOB_NAME = "jobForDbToExcelJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_DB_TO_EXCEL)
    public Job jobForDbToExcel(JobRepository jobRepository,
                               Step chunkStepForDbToExcelJob,
                               Step uploaderStepForDbToExcelJob,
                               Step tmpFileDeleteStepForDbToExcelJob,
                               Step logStepForDbToExcelJob,
                               JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForDbToExcelJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForDbToExcelJob)
                .next(uploaderStepForDbToExcelJob)
                .next(tmpFileDeleteStepForDbToExcelJob)
                .next(logStepForDbToExcelJob)
                .listener(jobExecutionStatusChangeListenerForDbToExcelJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForDbToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToExcelJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends BatchItem> Step chunkStepForDbToExcelJob(JobRepository jobRepository,
                                                               ArilBatchProperties arilBatchProperties,
                                                               PlatformTransactionManager transactionManager,
                                                               BatchJobSummaryService arilBatchJobSummaryService,
                                                               @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToExcelJobParameter> inputParam,
                                                               @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<BatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForDbToExcelJob(inputParam))
                .processor((ItemProcessor<? super BatchItem, ? extends D>) processorForDbToExcelJob(inputParam))
                .writer(writerForDbToExcelJob(inputParam, identifier))
                .listener(new ChunkStepExecutionListener(BatchType.DB_TO_EXCEL, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .readerIsTransactionalQueue()//to create a reader that does not buffer items in Java
                .build();
    }

    @Bean
    @JobScope
    public Step uploaderStepForDbToExcelJob(JobRepository jobRepository,
                                            FileUploader fileUploader,
                                            PlatformTransactionManager transactionManager,
                                            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToExcelJobParameter> inputParam,
                                            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(FILE_UPLOADER_STEP_NAME, jobRepository)
                .tasklet(new ExcelFileUploaderTasklet(fileUploader, inputParam.getJobParameter().getExportBucket(), exportFolder, JobUtils.getExcelFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step tmpFileDeleteStepForDbToExcelJob(JobRepository jobRepository,
                                                 PlatformTransactionManager transactionManager,
                                                 @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(TMP_FILE_DELETE_STEP_NAME, jobRepository)
                .tasklet(new LocalFileDeleteTasklet(JobUtils.getExcelFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step logStepForDbToExcelJob(JobRepository jobRepository,
                                       FileUploader fileUploaderAdapter,
                                       BatchJobSummaryService arilBatchJobSummaryService,
                                       PlatformTransactionManager transactionManager,
                                       @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToExcelJobParameter> inputParam,
                                       @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                       @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(LOG_STEP_NAME, jobRepository)
                .tasklet(new ExportMetadataTasklet(fileUploaderAdapter, arilBatchJobSummaryService, exportFolder, JobUtils.getExcelFilenameById(identifier), inputParam.getJobParameter().getExportBucket()), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public <T extends BatchItem> RepositoryItemReader<T> readerForDbToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToExcelJobParameter> inputParam) {
        return configureRepositoryItemReader(
                READER_NAME,
                inputParam.getJobParameter().getRepositoryItemReader(),
                inputParam.getJobParameter().getChunkSize()
        );
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends BatchItem> ItemProcessor<T, T> processorForDbToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToExcelJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends BatchItem> ExcelStreamItemWriter<D> writerForDbToExcelJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToExcelJobParameter> inputParam,
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
