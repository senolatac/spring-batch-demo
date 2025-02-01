package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.FileUploader;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchDbToCsvJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.core.tasklet.export.ExportMetadataTasklet;
import com.aril.arilbatchsdk.core.tasklet.file.CsvFileUploaderTasklet;
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
import org.springframework.batch.item.file.FlatFileItemWriter;
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
public class ArilBatchFromDbToCsvConfig extends BaseArilBatchConfig {

    public static final String READER_NAME = "readerForDbToCsvJob";
    private static final String WRITER_NAME = "writerForDbToCsvJob";
    private static final String CHUNK_STEP_NAME = "chunkStepForDbToCsvJob";
    private static final String LOG_STEP_NAME = "logStepForDbToCsvJob";
    private static final String FILE_UPLOADER_STEP_NAME = "fileUploaderStepForDbToCsvJob";
    private static final String TMP_FILE_DELETE_STEP_NAME = "tmpFileDeleteStepForDbToCsvJob";
    private static final String JOB_NAME = "jobForDbToCsvJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_DB_TO_CSV)
    public Job jobForDbToCsv(JobRepository jobRepository,
                             Step chunkStepForDbToCsvJob,
                             Step logStepForDbToCsvJob,
                             Step uploaderStepForDbToCsvJob,
                             Step tmpFileDeleteStepForDbToCsvJob,
                             JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForDbToCsvJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForDbToCsvJob)
                .next(logStepForDbToCsvJob)
                .next(uploaderStepForDbToCsvJob)
                .next(tmpFileDeleteStepForDbToCsvJob)
                .listener(jobExecutionStatusChangeListenerForDbToCsvJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForDbToCsvJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToCsvJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    public Step uploaderStepForDbToCsvJob(JobRepository jobRepository,
                                          FileUploader fileUploader,
                                          PlatformTransactionManager transactionManager,
                                          @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToCsvJobParameter> inputParam,
                                          @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                          @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(FILE_UPLOADER_STEP_NAME, jobRepository)
                .tasklet(new CsvFileUploaderTasklet(fileUploader, inputParam.getJobParameter().getExportBucket(), exportFolder, JobUtils.getCsvFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step tmpFileDeleteStepForDbToCsvJob(JobRepository jobRepository,
                                               PlatformTransactionManager transactionManager,
                                               @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(TMP_FILE_DELETE_STEP_NAME, jobRepository)
                .tasklet(new LocalFileDeleteTasklet(JobUtils.getCsvFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step logStepForDbToCsvJob(JobRepository jobRepository,
                                     FileUploader fileUploaderAdapter,
                                     BatchJobSummaryService arilBatchJobSummaryService,
                                     PlatformTransactionManager transactionManager,
                                     @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToCsvJobParameter> inputParam,
                                     @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                     @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(LOG_STEP_NAME, jobRepository)
                .tasklet(new ExportMetadataTasklet(fileUploaderAdapter, arilBatchJobSummaryService, exportFolder, JobUtils.getCsvFilenameById(identifier), inputParam.getJobParameter().getExportBucket()), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends BatchItem> Step chunkStepForDbToCsvJob(JobRepository jobRepository,
                                                             ArilBatchProperties arilBatchProperties,
                                                             PlatformTransactionManager transactionManager,
                                                             BatchJobSummaryService arilBatchJobSummaryService,
                                                             @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToCsvJobParameter> inputParam,
                                                             @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<BatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForDbToCsvJob(inputParam))
                .processor((ItemProcessor<? super BatchItem, ? extends D>) processorForDbToCsvJob(inputParam))
                .writer(writerForDbToCsvJob(inputParam, identifier))
                .listener(new ChunkStepExecutionListener(BatchType.DB_TO_CSV, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .readerIsTransactionalQueue()//to create a reader that does not buffer items in Java
                .build();
    }

    @Bean
    @StepScope
    public <T extends BatchItem> RepositoryItemReader<T> readerForDbToCsvJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToCsvJobParameter> inputParam) {
        return configureRepositoryItemReader(
                READER_NAME,
                inputParam.getJobParameter().getRepositoryItemReader(),
                inputParam.getJobParameter().getChunkSize()
        );
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends BatchItem> ItemProcessor<T, T> processorForDbToCsvJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToCsvJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends BatchItem> FlatFileItemWriter<D> writerForDbToCsvJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToCsvJobParameter> inputParam,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return configureFlatFileItemWriter(
                WRITER_NAME,
                identifier,
                inputParam.getJobParameter().getOutputHeader(),
                inputParam.getJobParameter().getOutputFields()
        );
    }
}
