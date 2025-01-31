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
import com.aril.arilbatchsdk.core.parameter.BatchCsvToKafkaJobParameter;
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
import org.springframework.batch.item.file.FlatFileItemReader;
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
public class ArilBatchFromCsvToKafkaConfig extends AbstractArilBatchConfig {

    public static final String READER_NAME = "readerForCsvToKafkaJob";
    public static final String CHUNK_STEP_NAME = "chunkStepForCsvToKafkaJob";
    private static final String LOG_STEP_NAME = "logStepForCsvToKafkaJob";
    private static final String JOB_NAME = "jobForCsvToKafkaJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_CSV_TO_KAFKA)
    public Job jobForCsvToKafkaJob(JobRepository jobRepository,
                                   Step chunkStepForCsvToKafkaJob,
                                   Step logStepForCsvToKafkaJob,
                                   JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForCsvToKafkaJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForCsvToKafkaJob)
                .next(logStepForCsvToKafkaJob)
                .listener(jobExecutionStatusChangeListenerForCsvToKafkaJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForCsvToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToKafkaJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentJobEvent<?>> Step chunkStepForCsvToKafkaJob(JobRepository jobRepository,
                                                                            ArilBatchProperties arilBatchProperties,
                                                                            PlatformTransactionManager transactionManager,
                                                                            KafkaItemIdempotentWriter<D> writerForCsvToKafkaJob,
                                                                            BatchJobSummaryService arilBatchJobSummaryService,
                                                                            IdempotentJobEventProcessorListener idempotentJobEventProcessorListener,
                                                                            ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentJobEvent<?>> processorForCsvToKafkaJob,
                                                                            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToKafkaJobParameter> inputParam) throws Exception {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<IdempotentBatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForCsvToKafkaJob(inputParam))
                .processor((ItemProcessor<? super IdempotentBatchItem, ? extends D>) processorForCsvToKafkaJob)
                .listener(idempotentJobEventProcessorListener)
                .writer(writerForCsvToKafkaJob)
                .listener(new ChunkStepExecutionListener(BatchType.CSV_TO_KAFKA, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .faultTolerant()
                .skip(ArilIdempotentException.class)
                .skipPolicy(new IdempotentSkipPolicy())
                .retryPolicy(new NeverRetryPolicy())
                .processorNonTransactional()
                .build();
    }

    @Bean
    @JobScope
    public Step logStepForCsvToKafkaJob(JobRepository jobRepository,
                                        BatchConsumerTrackerFacade batchConsumerTrackerFacade,
                                        PlatformTransactionManager transactionManager,
                                        @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToKafkaJobParameter> inputParam) {
        return new StepBuilder(LOG_STEP_NAME, jobRepository)
                .tasklet(new ConsumerTrackerTasklet(batchConsumerTrackerFacade, inputParam.getJobParameter().getTopic(), inputParam.getJobParameter().getConsumerCompletedNotifierTopic(), CHUNK_STEP_NAME), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public <T extends IdempotentBatchItem> FlatFileItemReader<T> readerForCsvToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToKafkaJobParameter> inputParam) throws Exception {
        BatchCsvToKafkaJobParameter jobParameter = inputParam.getJobParameter();
        return configureFlatFileItemReader(READER_NAME, jobParameter.getResource(), jobParameter.getInputFields(), jobParameter.getInputItemClass());
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <I extends IdempotentBatchItem, O extends IdempotentJobEvent<?>> ItemProcessor<I, O> processorForCsvToKafkaJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToKafkaJobParameter> inputParam,
            IdempotentJobEventProcessor idempotentJobEventProcessor) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<I, O>) inputParam.getJobParameter().getItemProcessor();
        }
        return (ItemProcessor<I, O>) idempotentJobEventProcessor;
    }

    @Bean
    @StepScope
    public <D extends IdempotentJobEvent<?>> KafkaItemIdempotentWriter<D> writerForCsvToKafkaJob(
            @Qualifier(BeanIds.ARIL_KAFKA_TEMPLATE) ArilKafkaTemplate<String, D> arilKafkaTemplate,
            ArilIdempotentTemplate arilIdempotentTemplate,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchCsvToKafkaJobParameter> inputParam) {
        return new KafkaItemIdempotentWriterBuilder<D>()
                .name(inputParam.getJobParameter().getJobName())
                .topic(inputParam.getJobParameter().getTopic())
                .idempotencyOptions(inputParam.getJobParameter().getIdempotencyOptions())
                .idempotentWriter(new IdempotentWriter(arilIdempotentTemplate))
                .kafkaTemplate(arilKafkaTemplate)
                .build();
    }
}
