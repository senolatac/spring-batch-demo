package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.item.service.IdempotentService;
import com.aril.arilbatchsdk.core.item.service.writer.ServiceItemIdempotentWriter;
import com.aril.arilbatchsdk.core.item.service.writer.ServiceItemIdempotentWriterBuilder;
import com.aril.arilbatchsdk.core.item.support.IdempotentWriter;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchDbToServiceJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryItemReaderParameter;
import com.aril.arilbatchsdk.core.skip.IdempotentSkipPolicy;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
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
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
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
@ConditionalOnClass({JpaRepository.class, ArilIdempotentTemplate.class})
public class ArilBatchFromDbToServiceConfig {

    public static final String READER_NAME = "readerForDbToServiceJob";
    private static final String CHUNK_STEP_NAME = "chunkStepForDbToServiceJob";
    private static final String JOB_NAME = "jobForDbToServiceJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_DB_TO_SERVICE)
    public Job jobForDbToServiceJob(JobRepository jobRepository,
                                    Step chunkStepForDbToServiceJob,
                                    JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForDbToServiceJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForDbToServiceJob)
                .listener(jobExecutionStatusChangeListenerForDbToServiceJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForDbToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToServiceJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentJobEvent<?>> Step chunkStepForDbToServiceJob(JobRepository jobRepository,
                                                                             ArilBatchProperties arilBatchProperties,
                                                                             PlatformTransactionManager transactionManager,
                                                                             ServiceItemIdempotentWriter<D> writerForDbToServiceJob,
                                                                             BatchJobSummaryService batchJobSummaryService,
                                                                             ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentBatchItem> processorForDbToServiceJob,
                                                                             @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToServiceJobParameter> inputParam) {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<IdempotentBatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForDbToServiceJob(inputParam))
                .processor((ItemProcessor<? super IdempotentBatchItem, ? extends D>) processorForDbToServiceJob)
                .writer(writerForDbToServiceJob)
                .listener(new ChunkStepExecutionListener(BatchType.DB_TO_SERVICE, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), batchJobSummaryService))
                .faultTolerant()
                .skip(ArilIdempotentException.class)
                .skipPolicy(new IdempotentSkipPolicy())
                .retryPolicy(new NeverRetryPolicy())
                .processorNonTransactional()
                .build();
    }

    @Bean
    @StepScope
    public <T extends IdempotentBatchItem> RepositoryItemReader<T> readerForDbToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToServiceJobParameter> inputParam) {
        RepositoryItemReaderParameter repositoryItemReader = inputParam.getJobParameter().getRepositoryItemReader();

        return new RepositoryItemReaderBuilder<T>()
                .name(READER_NAME)
                .repository(repositoryItemReader.getRepository())
                .methodName(repositoryItemReader.getMethodName())
                .pageSize(inputParam.getJobParameter().getChunkSize())
                .arguments(repositoryItemReader.getArguments())
                .sorts(repositoryItemReader.getSorts())
                .build();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends IdempotentBatchItem> ItemProcessor<T, T> processorForDbToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToServiceJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentBatchItem> ServiceItemIdempotentWriter<D> writerForDbToServiceJob(
            ArilIdempotentTemplate arilIdempotentTemplate,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToServiceJobParameter> inputParam) {
        BatchDbToServiceJobParameter jobParameter = inputParam.getJobParameter();

        return new ServiceItemIdempotentWriterBuilder<D>()
                .name(jobParameter.getJobName())
                .service((IdempotentService<D>) jobParameter.getServiceItemWriter().getService())
                .methodName(jobParameter.getServiceItemWriter().getMethodName())
                .arguments(jobParameter.getServiceItemWriter().getArguments())
                .idempotencyOptions(jobParameter.getIdempotencyOptions())
                .idempotentWriter(new IdempotentWriter(arilIdempotentTemplate))
                .build();
    }
}
