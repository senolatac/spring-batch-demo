package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.item.kafka.KafkaItemIdempotentWriter;
import com.aril.arilbatchsdk.core.item.service.reader.ServiceItemReader;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.IdempotentJobEventProcessorListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchServiceToKafkaJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Lazy
@Configuration
@EnableArilKafka
@EnableArilIdempotent
@ConditionalOnClass({ArilKafkaTemplate.class, ArilIdempotentTemplate.class})
public class ArilBatchFromServiceToKafkaConfig extends BaseArilBatchKafkaConfig {

    public static final String READER_NAME = "readerForServiceToKafkaJob";
    public static final String CHUNK_STEP_NAME = "chunkStepForServiceToKafkaJob";
    private static final String LOG_STEP_NAME = "logStepForServiceToKafkaJob";
    private static final String JOB_NAME = "jobForServiceToKafkaJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_SERVICE_TO_KAFKA)
    public Job jobForServiceToKafkaJob(JobRepository jobRepository,
                                       Step chunkStepForServiceToKafkaJob,
                                       Step logStepForServiceToKafkaJob,
                                       JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForServiceToKafkaJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForServiceToKafkaJob)
                .next(logStepForServiceToKafkaJob)
                .listener(jobExecutionStatusChangeListenerForServiceToKafkaJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForServiceToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToKafkaJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentJobEvent<?>> Step chunkStepForServiceToKafkaJob(JobRepository jobRepository,
                                                                                ArilBatchProperties arilBatchProperties,
                                                                                PlatformTransactionManager transactionManager,
                                                                                KafkaItemIdempotentWriter<D> writerForServiceToKafkaJob,
                                                                                BatchJobSummaryService arilBatchJobSummaryService,
                                                                                IdempotentJobEventProcessorListener idempotentJobEventProcessorListener,
                                                                                ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentJobEvent<?>> processorForServiceToKafkaJob,
                                                                                @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToKafkaJobParameter> inputParam) {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<IdempotentBatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForServiceToKafkaJob(inputParam))
                .processor((ItemProcessor<? super IdempotentBatchItem, ? extends D>) processorForServiceToKafkaJob)
                .listener(idempotentJobEventProcessorListener)
                .writer(writerForServiceToKafkaJob)
                .listener(new ChunkStepExecutionListener(BatchType.SERVICE_TO_KAFKA, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .faultTolerant()
                .skip(ArilIdempotentException.class)
                .skipPolicy(new IdempotentSkipPolicy())
                .retryPolicy(new NeverRetryPolicy())
                .processorNonTransactional()
                .build();
    }

    @Bean
    @JobScope
    public Step logStepForServiceToKafkaJob(JobRepository jobRepository,
                                            BatchConsumerTrackerFacade batchConsumerTrackerFacade,
                                            PlatformTransactionManager transactionManager,
                                            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToKafkaJobParameter> inputParam) {
        return new StepBuilder(LOG_STEP_NAME, jobRepository)
                .tasklet(new ConsumerTrackerTasklet(batchConsumerTrackerFacade, inputParam.getJobParameter().getTopic(), inputParam.getJobParameter().getConsumerCompletedNotifierTopic(), CHUNK_STEP_NAME), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends IdempotentBatchItem> ServiceItemReader<T> readerForServiceToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToKafkaJobParameter> inputParam) {
        return (ServiceItemReader<T>) inputParam.getJobParameter().getServiceItemReader();
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <I extends IdempotentBatchItem, O extends IdempotentJobEvent<?>> ItemProcessor<I, O> processorForServiceToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToKafkaJobParameter> inputParam,
            IdempotentJobEventProcessor idempotentJobEventProcessor) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<I, O>) inputParam.getJobParameter().getItemProcessor();
        }
        return (ItemProcessor<I, O>) idempotentJobEventProcessor;
    }

    @Bean
    @StepScope
    public <D extends IdempotentJobEvent<?>> KafkaItemIdempotentWriter<D> writerForServiceToKafkaJob(
            @Qualifier(BeanIds.ARIL_KAFKA_TEMPLATE) ArilKafkaTemplate<String, D> arilKafkaTemplate,
            ArilIdempotentTemplate arilIdempotentTemplate,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchServiceToKafkaJobParameter> inputParam) {
        return configureKafkaItemIdempotentWriter(
                inputParam.getJobParameter().getJobName(),
                inputParam.getJobParameter().getTopic(),
                inputParam.getJobParameter().getIdempotencyOptions(),
                arilIdempotentTemplate,
                arilKafkaTemplate
        );
    }
}

