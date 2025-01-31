package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.item.data.RepositoryItemIdempotentWriter;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchExcelToDbJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.core.skip.IdempotentSkipPolicy;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import com.aril.arilidempotentsdk.annotation.EnableArilIdempotent;
import com.aril.arilidempotentsdk.core.ArilIdempotentTemplate;
import com.aril.arilidempotentsdk.exception.ArilIdempotentException;
import com.aril.valhala.batch.IdempotentBatchItem;
import com.aril.valhala.event.IdempotentJobEvent;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.extensions.excel.streaming.StreamingXlsxItemReader;
import org.springframework.batch.item.ItemProcessor;
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
public class ArilBatchFromExcelToDbConfig extends AbstractArilBatchConfig {

    public static final String READER_NAME = "readerForExcelToDbJob";
    public static final String CHUNK_STEP_NAME = "chunkStepForExcelToDbJob";
    private static final String JOB_NAME = "jobForExcelToDbJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_EXCEL_TO_DB)
    public Job jobForExcelToDbJob(JobRepository jobRepository,
                                  Step chunkStepForExcelToDbJob,
                                  JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForExcelToDbJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForExcelToDbJob)
                .listener(jobExecutionStatusChangeListenerForExcelToDbJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForExcelToDbJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToDbJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentJobEvent<?>> Step chunkStepForExcelToDbJob(JobRepository jobRepository,
                                                                           ArilBatchProperties arilBatchProperties,
                                                                           PlatformTransactionManager transactionManager,
                                                                           RepositoryItemIdempotentWriter<D> writerForExcelToDbJob,
                                                                           BatchJobSummaryService arilBatchJobSummaryService,
                                                                           ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentBatchItem> processorForExcelToDbJob,
                                                                           @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToDbJobParameter> inputParam) throws Exception {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<IdempotentBatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForExcelToDbJob(inputParam))
                .processor((ItemProcessor<? super IdempotentBatchItem, ? extends D>) processorForExcelToDbJob)
                .writer(writerForExcelToDbJob)
                .listener(new ChunkStepExecutionListener(BatchType.EXCEL_TO_DB, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .faultTolerant()
                .skip(ArilIdempotentException.class)
                .skipPolicy(new IdempotentSkipPolicy())
                .retryPolicy(new NeverRetryPolicy())
                .processorNonTransactional()
                .build();
    }

    @Bean
    @StepScope
    public <T extends IdempotentBatchItem> StreamingXlsxItemReader<T> readerForExcelToDbJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToDbJobParameter> inputParam) throws Exception {
        BatchExcelToDbJobParameter jobParameter = inputParam.getJobParameter();
        return configureStreamingXlsxItemReader(READER_NAME, jobParameter.getResource(), jobParameter.getInputFields(), jobParameter.getItemInputClass());
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends IdempotentBatchItem> ItemProcessor<T, T> processorForExcelToDbJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToDbJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends IdempotentBatchItem> RepositoryItemIdempotentWriter<D> writerForExcelToDbJob(
            ArilIdempotentTemplate arilIdempotentTemplate,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToDbJobParameter> inputParam) {
        return configureRepositoryItemIdempotentWriter(
                inputParam.getJobParameter().getJobName(),
                inputParam.getJobParameter().getRepositoryItemWriter(),
                inputParam.getJobParameter().getIdempotencyOptions(),
                arilIdempotentTemplate
        );
    }
}
