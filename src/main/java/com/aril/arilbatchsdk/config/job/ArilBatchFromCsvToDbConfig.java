package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.item.data.RepositoryItemIdempotentWriter;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchCsvToDbJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.core.skip.IdempotentSkipPolicy;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import com.aril.arilidempotentsdk.annotation.EnableArilIdempotent;
import com.aril.arilidempotentsdk.core.ArilIdempotentTemplate;
import com.aril.arilidempotentsdk.exception.ArilIdempotentException;
import com.aril.valhala.batch.IdempotentBatchItem;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Lazy
@Configuration
@EnableArilIdempotent
@ConditionalOnClass({JpaRepository.class, ArilIdempotentTemplate.class})
public class ArilBatchFromCsvToDbConfig extends AbstractArilBatchConfig {

    public static final String READER_NAME = "readerForCsvToDbJob";
    private static final String CHUNK_STEP_NAME = "chunkStepForCsvToDbJob";
    private static final String JOB_NAME = "jobForCsvToDbJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_CSV_TO_DB)
    public Job jobForCsvToDb(JobRepository jobRepository,
                             Step chunkStepForCsvToDbJob,
                             JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForCsvToDbJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForCsvToDbJob)
                .listener(jobExecutionStatusChangeListenerForCsvToDbJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForCsvToDbJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToDbJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentBatchItem> Step chunkStepForCsvToDbJob(JobRepository jobRepository,
                                                                       ArilBatchProperties arilBatchProperties,
                                                                       PlatformTransactionManager transactionManager,
                                                                       BatchJobSummaryService arilBatchJobSummaryService,
                                                                       RepositoryItemIdempotentWriter<D> writerForCsvToDbJob,
                                                                       @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToDbJobParameter> inputParam) throws Exception {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<IdempotentBatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForCsvToDbJob(inputParam))
                .processor((ItemProcessor<? super IdempotentBatchItem, ? extends D>) processorForCsvToDbJob(inputParam))
                .writer(writerForCsvToDbJob)
                .listener(new ChunkStepExecutionListener(BatchType.CSV_TO_DB, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .faultTolerant()
                .skip(ArilIdempotentException.class)
                .skipPolicy(new IdempotentSkipPolicy())
                .retryPolicy(new NeverRetryPolicy())
                .processorNonTransactional()
                .build();
    }

    @Bean
    @StepScope
    public <T extends IdempotentBatchItem> FlatFileItemReader<T> readerForCsvToDbJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToDbJobParameter> inputParam) throws Exception {
        BatchCsvToDbJobParameter jobParameter = inputParam.getJobParameter();
        return configureFlatFileItemReader(READER_NAME, jobParameter.getResource(), jobParameter.getInputFields(), jobParameter.getInputItemClass());
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends IdempotentBatchItem> ItemProcessor<T, T> processorForCsvToDbJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToDbJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends IdempotentBatchItem> RepositoryItemIdempotentWriter<D> writerForCsvToDbJob(
            ArilIdempotentTemplate arilIdempotentTemplate,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToDbJobParameter> inputParam) {
        return configureRepositoryItemIdempotentWriter(
                inputParam.getJobParameter().getJobName(),
                inputParam.getJobParameter().getRepositoryItemWriter(),
                inputParam.getJobParameter().getIdempotencyOptions(),
                arilIdempotentTemplate
        );
    }
}

