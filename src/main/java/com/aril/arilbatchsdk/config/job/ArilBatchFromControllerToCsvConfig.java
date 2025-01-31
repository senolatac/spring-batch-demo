package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.FileUploader;
import com.aril.arilbatchsdk.core.item.controller.ControllerItemReader;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchControllerToCsvJobParameter;
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
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
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
public class ArilBatchFromControllerToCsvConfig extends AbstractArilBatchConfig {

    public static final String READER_NAME = "readerForControllerToCsvJob";
    private static final String WRITER_NAME = "writerForControllerToCsvJob";
    private static final String CHUNK_STEP_NAME = "chunkStepForControllerToCsvJob";
    private static final String LOG_STEP_NAME = "logStepForControllerToCsvJob";
    private static final String FILE_UPLOADER_STEP_NAME = "fileUploaderForControllerToCsvJob";
    private static final String TMP_FILE_DELETE_STEP_NAME = "tmpFileDeleteForControllerToCsvJob";
    private static final String JOB_NAME = "jobForControllerToCsvJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_CONTROLLER_TO_CSV)
    public Job jobForControllerToCsvJob(JobRepository jobRepository,
                                        Step chunkStepForControllerToCsvJob,
                                        Step uploaderStepForControllerToCsvJob,
                                        Step tmpFileDeleteStepForControllerToCsvJob,
                                        Step logStepForControllerToCsvJob,
                                        JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForControllerToCsvJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForControllerToCsvJob)
                .next(uploaderStepForControllerToCsvJob)
                .next(tmpFileDeleteStepForControllerToCsvJob)
                .next(logStepForControllerToCsvJob)
                .listener(jobExecutionStatusChangeListenerForControllerToCsvJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForControllerToCsvJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToCsvJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends BatchItem> Step chunkStepForControllerToCsvJob(JobRepository jobRepository,
                                                                     ArilBatchProperties arilBatchProperties,
                                                                     PlatformTransactionManager transactionManager,
                                                                     BatchJobSummaryService arilBatchJobSummaryService,
                                                                     @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToCsvJobParameter> inputParam,
                                                                     @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<BatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForControllerToCsvJob(inputParam))
                .processor((ItemProcessor<? super BatchItem, ? extends D>) processorForControllerToCsvJob(inputParam))
                .writer(writerForControllerToCsvJob(inputParam, identifier))
                .listener(new ChunkStepExecutionListener(BatchType.CONTROLLER_TO_CSV, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .readerIsTransactionalQueue()//to create a reader that does not buffer items in Java
                .build();
    }

    @Bean
    @JobScope
    public Step uploaderStepForControllerToCsvJob(JobRepository jobRepository,
                                                  FileUploader fileUploader,
                                                  PlatformTransactionManager transactionManager,
                                                  @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToCsvJobParameter> inputParam,
                                                  @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                                  @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(FILE_UPLOADER_STEP_NAME, jobRepository)
                .tasklet(new CsvFileUploaderTasklet(fileUploader, inputParam.getJobParameter().getExportBucket(), exportFolder, JobUtils.getCsvFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step tmpFileDeleteStepForControllerToCsvJob(JobRepository jobRepository,
                                                       PlatformTransactionManager transactionManager,
                                                       @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new StepBuilder(TMP_FILE_DELETE_STEP_NAME, jobRepository)
                .tasklet(new LocalFileDeleteTasklet(JobUtils.getCsvFilenameById(identifier)), transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public Step logStepForControllerToCsvJob(JobRepository jobRepository,
                                             FileUploader fileUploader,
                                             BatchJobSummaryService arilBatchJobSummaryService,
                                             PlatformTransactionManager transactionManager,
                                             @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToCsvJobParameter> inputParam,
                                             @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier,
                                             @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).JOB_EXPORT_FOLDER]}") String exportFolder) {
        return new StepBuilder(LOG_STEP_NAME, jobRepository)
                .tasklet(new ExportMetadataTasklet(fileUploader, arilBatchJobSummaryService, exportFolder, JobUtils.getCsvFilenameById(identifier), inputParam.getJobParameter().getExportBucket()), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends BatchItem> ControllerItemReader<T> readerForControllerToCsvJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToCsvJobParameter> inputParam) {
        return (ControllerItemReader<T>) inputParam.getJobParameter().getControllerItemReader();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends BatchItem> ItemProcessor<T, T> processorForControllerToCsvJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToCsvJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends BatchItem> FlatFileItemWriter<D> writerForControllerToCsvJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchControllerToCsvJobParameter> inputParam,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM_IDENTIFIER]}") String identifier) {
        return new FlatFileItemWriterBuilder<D>()
                .name(WRITER_NAME)
                .resource(new FileSystemResource(JobUtils.getCsvFilenameById(identifier)))
                .headerCallback(writer -> writer.write(inputParam.getJobParameter().getOutputHeader()))
                .lineAggregator(delimitedLineAggregator(inputParam.getJobParameter().getOutputFields()))
                .build();
    }
}
