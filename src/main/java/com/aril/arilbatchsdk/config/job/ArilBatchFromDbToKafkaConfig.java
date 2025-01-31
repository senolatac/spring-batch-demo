package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.item.kafka.KafkaItemIdempotentWriter;
import com.aril.arilbatchsdk.core.item.kafka.KafkaItemIdempotentWriterBuilder;
import com.aril.arilbatchsdk.core.item.support.IdempotentWriter;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.IdempotentJobEventProcessorListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchDbToKafkaJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryItemReaderParameter;
import com.aril.arilbatchsdk.core.processor.IdempotentJobEventProcessor;
import com.aril.arilbatchsdk.core.skip.IdempotentSkipPolicy;
import com.aril.arilbatchsdk.core.tasklet.consumer.ConsumerTrackerTasklet;
import com.aril.arilbatchsdk.facade.BatchConsumerTrackerFacade;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import com.aril.arilidempotentsdk.annotation.EnableArilIdempotent;
import com.aril.arilidempotentsdk.core.ArilIdempotentTemplate;
import com.aril.arilidempotentsdk.exception.ArilIdempotentException;
import com.aril.arilkafka.annotation.EnableArilKafka;
import com.aril.arilkafka.config.BeanIds;
import com.aril.arilkafka.core.ArilKafkaTemplate;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
@EnableArilKafka
@EnableArilIdempotent
@ConditionalOnClass({JpaRepository.class, ArilKafkaTemplate.class, ArilIdempotentTemplate.class})
public class ArilBatchFromDbToKafkaConfig {

    public static final String READER_NAME = "readerForDbToKafkaJob";
    public static final String CHUNK_STEP_NAME = "chunkStepForDbToKafkaJob";
    private static final String LOG_STEP_NAME = "logStepForDbToKafkaJob";
    private static final String JOB_NAME = "jobForDbToKafkaJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_DB_TO_KAFKA)
    public Job jobForDbToKafkaJob(JobRepository jobRepository,
                                  Step chunkStepForDbToKafkaJob,
                                  Step logStepForDbToKafkaJob,
                                  JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForDbToKafkaJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForDbToKafkaJob)
                .next(logStepForDbToKafkaJob)
                .listener(jobExecutionStatusChangeListenerForDbToKafkaJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForDbToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToKafkaJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentJobEvent<?>> Step chunkStepForDbToKafkaJob(JobRepository jobRepository,
                                                                           ArilBatchProperties arilBatchProperties,
                                                                           PlatformTransactionManager transactionManager,
                                                                           KafkaItemIdempotentWriter<D> writerForDbToKafkaJob,
                                                                           BatchJobSummaryService arilBatchJobSummaryService,
                                                                           IdempotentJobEventProcessorListener idempotentJobEventProcessorListener,
                                                                           ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentJobEvent<?>> processorForDbToKafkaJob,
                                                                           @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToKafkaJobParameter> inputParam) {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<IdempotentBatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForDbToKafkaJob(inputParam))
                .processor((ItemProcessor<? super IdempotentBatchItem, ? extends D>) processorForDbToKafkaJob)
                .listener(idempotentJobEventProcessorListener)
                .writer(writerForDbToKafkaJob)
                .listener(new ChunkStepExecutionListener(BatchType.DB_TO_KAFKA, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .faultTolerant()
                .skip(ArilIdempotentException.class)
                .skipPolicy(new IdempotentSkipPolicy())
                .retryPolicy(new NeverRetryPolicy())
                .processorNonTransactional()
                .build();
    }

    @Bean
    @JobScope
    public Step logStepForDbToKafkaJob(JobRepository jobRepository,
                                       BatchConsumerTrackerFacade batchConsumerTrackerFacade,
                                       PlatformTransactionManager transactionManager,
                                       @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToKafkaJobParameter> inputParam) {
        return new StepBuilder(LOG_STEP_NAME, jobRepository)
                .tasklet(new ConsumerTrackerTasklet(batchConsumerTrackerFacade, inputParam.getJobParameter().getTopic(), inputParam.getJobParameter().getConsumerCompletedNotifierTopic(), CHUNK_STEP_NAME), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public <T extends IdempotentBatchItem> RepositoryItemReader<T> readerForDbToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToKafkaJobParameter> inputParam) {
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
    public <I extends IdempotentBatchItem, O extends IdempotentJobEvent<?>> ItemProcessor<I, O> processorForDbToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToKafkaJobParameter> inputParam,
            IdempotentJobEventProcessor idempotentJobEventProcessor) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<I, O>) inputParam.getJobParameter().getItemProcessor();
        }
        return (ItemProcessor<I, O>) idempotentJobEventProcessor;
    }

    @Bean
    @StepScope
    public <D extends IdempotentJobEvent<?>> KafkaItemIdempotentWriter<D> writerForDbToKafkaJob(
            @Qualifier(BeanIds.ARIL_KAFKA_TEMPLATE) ArilKafkaTemplate<String, D> arilKafkaTemplate,
            ArilIdempotentTemplate arilIdempotentTemplate,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchDbToKafkaJobParameter> inputParam) {
        return new KafkaItemIdempotentWriterBuilder<D>()
                .name(inputParam.getJobParameter().getJobName())
                .topic(inputParam.getJobParameter().getTopic())
                .idempotencyOptions(inputParam.getJobParameter().getIdempotencyOptions())
                .idempotentWriter(new IdempotentWriter(arilIdempotentTemplate))
                .kafkaTemplate(arilKafkaTemplate)
                .build();
    }
}
