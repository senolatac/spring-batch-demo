package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.item.service.writer.ServiceItemIdempotentWriter;
import com.aril.arilbatchsdk.core.listener.ChunkStepExecutionListener;
import com.aril.arilbatchsdk.core.listener.JobExecutionStatusChangeListener;
import com.aril.arilbatchsdk.core.parameter.BatchExcelToServiceJobParameter;
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
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

@Lazy
@Configuration
@EnableArilIdempotent
@ConditionalOnClass({ArilIdempotentTemplate.class})
public class ArilBatchFromExcelToServiceConfig extends BaseArilBatchIdempotentConfig {

    public static final String READER_NAME = "readerForExcelToServiceJob";
    public static final String CHUNK_STEP_NAME = "chunkStepForExcelToServiceJob";
    private static final String JOB_NAME = "jobForExcelToServiceJob";

    @Bean(ArilBatchConfigConstants.JOB_FOR_EXCEL_TO_SERVICE)
    public Job jobForExcelToServiceJob(JobRepository jobRepository,
                                       Step chunkStepForExcelToServiceJob,
                                       JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForExcelToServiceJob) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStepForExcelToServiceJob)
                .listener(jobExecutionStatusChangeListenerForExcelToServiceJob)
                .build();
    }

    @Bean
    @JobScope
    public JobExecutionStatusChangeListener jobExecutionStatusChangeListenerForExcelToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToServiceJobParameter> inputParam
    ) {
        return new JobExecutionStatusChangeListener(inputParam.getJobParameter().getJobExecutionChangeSubscriber());
    }

    @Bean
    @JobScope
    @SuppressWarnings("unchecked")
    public <D extends IdempotentJobEvent<?>> Step chunkStepForExcelToServiceJob(JobRepository jobRepository,
                                                                                ArilBatchProperties arilBatchProperties,
                                                                                PlatformTransactionManager transactionManager,
                                                                                ServiceItemIdempotentWriter<D> writerForExcelToServiceJob,
                                                                                BatchJobSummaryService arilBatchJobSummaryService,
                                                                                ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentBatchItem> processorForExcelToServiceJob,
                                                                                @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToServiceJobParameter> inputParam) throws Exception {
        return new StepBuilder(CHUNK_STEP_NAME, jobRepository)
                .<IdempotentBatchItem, D>chunk(inputParam.getJobParameter().getChunkSize(), transactionManager)
                .reader(readerForExcelToServiceJob(inputParam))
                .processor((ItemProcessor<? super IdempotentBatchItem, ? extends D>) processorForExcelToServiceJob)
                .writer(writerForExcelToServiceJob)
                .listener(new ChunkStepExecutionListener(BatchType.EXCEL_TO_SERVICE, arilBatchProperties.getActiveModule(), inputParam.getJobParameter(), arilBatchJobSummaryService))
                .faultTolerant()
                .skip(ArilIdempotentException.class)
                .skipPolicy(new IdempotentSkipPolicy())
                .retryPolicy(new NeverRetryPolicy())
                .processorNonTransactional()
                .build();
    }

    @Bean
    @StepScope
    public <T extends IdempotentBatchItem> StreamingXlsxItemReader<T> readerForExcelToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToServiceJobParameter> inputParam) throws Exception {
        BatchExcelToServiceJobParameter jobParameter = inputParam.getJobParameter();
        return configureStreamingXlsxItemReader(READER_NAME, jobParameter.getResource(), jobParameter.getInputFields(), jobParameter.getItemInputClass());
    }

    @Bean
    @StepScope
    @SuppressWarnings("unchecked")
    public <T extends IdempotentBatchItem> ItemProcessor<T, T> processorForExcelToServiceJob(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToServiceJobParameter> inputParam) {
        if (inputParam.getJobParameter().getItemProcessor() != null) {
            return (ItemProcessor<T, T>) inputParam.getJobParameter().getItemProcessor();
        }
        return item -> item;
    }

    @Bean
    @StepScope
    public <D extends IdempotentBatchItem> ServiceItemIdempotentWriter<D> writerForExcelToServiceJob(
            ArilIdempotentTemplate arilIdempotentTemplate,
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<BatchExcelToServiceJobParameter> inputParam) {
        return configureServiceItemIdempotentWriter(
                inputParam.getJobParameter().getJobName(),
                inputParam.getJobParameter().getServiceItemWriter(),
                inputParam.getJobParameter().getIdempotencyOptions(),
                arilIdempotentTemplate
        );
    }
}
