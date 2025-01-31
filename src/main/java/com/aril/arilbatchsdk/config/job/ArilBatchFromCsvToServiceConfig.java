package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.item.service.writer.ServiceItemIdempotentWriter;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchCsvToServiceJobParameter;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Lazy
@Configuration
@EnableArilIdempotent
@ConditionalOnClass({ArilIdempotentTemplate.class})
public class ArilBatchFromCsvToServiceConfig extends AbstractArilBatchConfig {

    public static final String READER_NAME = "readerForCsvToServiceJob";
    private static final String CHUNK_STEP_NAME = "chunkStepForCsvToServiceJob";
    private static final String JOB_NAME = "jobForCsvToServiceJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_CSV_TO_SERVICE)
    public Job jobForCsvToService(JobRepository jobRepository,
                                  Step chunkStepForCsvToServiceJob,
                                  JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForCsvToServiceJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForCsvToServiceJob)
                .listener(jobExecutionStatusChangeListenerForCsvToServiceJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForCsvToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToServiceJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentJobEvent<?>> Step chunkStepForCsvToServiceJob(JobRepository jobRepository,
                                                                              ArilBatchProperties arilBatchProperties,
                                                                              PlatformTransactionManager transactionManager,
                                                                              ServiceItemIdempotentWriter<D> writerForCsvToServiceJob,
                                                                              BatchJobSummaryService batchJobSummaryService,
                                                                              ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentBatchItem> processorForCsvToServiceJob,
                                                                              @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToServiceJobParameter> inputParam) throws Exception {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<IdempotentBatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForCsvToServiceJob(inputParam))
                .processor((ItemProcessor<? super IdempotentBatchItem, ? extends D>) processorForCsvToServiceJob)
                .writer(writerForCsvToServiceJob)
                .listener(new ChunkStepExecutionListener(BatchType.CSV_TO_SERVICE, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), batchJobSummaryService))
                .faultTolerant()
                .skip(ArilIdempotentException.class)
                .skipPolicy(new IdempotentSkipPolicy())
                .retryPolicy(new NeverRetryPolicy())
                .processorNonTransactional()
                .build();
    }

    @Bean
    @StepScope
    public <T extends IdempotentBatchItem> FlatFileItemReader<T> readerForCsvToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToServiceJobParameter> inputParam) throws Exception {
        BatchCsvToServiceJobParameter jobParameter = inputParam.getJobParameter();
        return configureFlatFileItemReader(READER_NAME, jobParameter.getResource(), jobParameter.getInputFields(), jobParameter.getInputItemClass());
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends IdempotentBatchItem> ItemProcessor<T, T> processorForCsvToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToServiceJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends IdempotentBatchItem> ServiceItemIdempotentWriter<D> writerForCsvToServiceJob(
            ArilIdempotentTemplate arilIdempotentTemplate,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToServiceJobParameter> inputParam) {
        return configureServiceItemIdempotentWriter(
                inputParam.getJobParameter().getJobName(),
                inputParam.getJobParameter().getServiceItemWriter(),
                inputParam.getJobParameter().getIdempotencyOptions(),
                arilIdempotentTemplate
        );
    }
}

