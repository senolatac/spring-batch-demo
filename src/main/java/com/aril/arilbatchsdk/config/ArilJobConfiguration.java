package com.aril.arilbatchsdk.config;

import com.aril.arilbatchsdk.core.converter.BatchJobParameterWrapperToStringConverter;
import com.aril.arilbatchsdk.core.converter.StringToBatchJobParameterWrapperConverter;
import com.aril.arilbatchsdk.core.explore.ArilJobExplorer;
import com.aril.arilbatchsdk.core.listener.IdempotentJobEventProcessorListener;
import com.aril.arilbatchsdk.core.parameter.BaseBatchJobParameter;
import com.aril.arilbatchsdk.core.parameter.support.BatchJobParameterWrapper;
import com.aril.arilbatchsdk.core.processor.IdempotentJobEventProcessor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.converter.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
@EnableConfigurationProperties(ArilBatchProperties.class)
@RequiredArgsConstructor
public class ArilJobConfiguration extends DefaultBatchConfiguration {

    private final ArilBatchProperties arilBatchProperties;


    @Bean(name = ArilBatchConfigConstants.ASYNC_JOB_LAUNCHER)
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) throws BatchConfigurationException {
        TaskExecutorJobLauncher taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(jobRepository);
        taskExecutorJobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        try {
            taskExecutorJobLauncher.afterPropertiesSet();
            return taskExecutorJobLauncher;
        } catch (Exception e) {
            throw new BatchConfigurationException("Unable to configure the async job launcher", e);
        }
    }

    @Bean
    public ArilJobExplorer arilJobExplorer(JobRepository jobRepository) {
        return new ArilJobExplorer(jobRepository);
    }

    @Bean
    @StepScope
    public IdempotentJobEventProcessor idempotentJobEventProcessor(
            @Value("#{jobParameters[T(com.aril.arilbatchsdk.config.ArilBatchConfigConstants).INPUT_PARAM]}") BatchJobParameterWrapper<? extends BaseBatchJobParameter> inputParam
    ) {
        return new IdempotentJobEventProcessor(inputParam.getJobParameter().getJobOwner());
    }

    @Bean
    @StepScope
    public IdempotentJobEventProcessorListener idempotentJobEventProcessorListener() {
        return new IdempotentJobEventProcessorListener();
    }

    @Override
    protected @NonNull String getTablePrefix() {
        return arilBatchProperties.getTablePrefix();
    }

    @Override
    protected @NonNull ConfigurableConversionService getConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new DateToStringConverter());
        conversionService.addConverter(new StringToDateConverter());
        conversionService.addConverter(new LocalDateToStringConverter());
        conversionService.addConverter(new StringToLocalDateConverter());
        conversionService.addConverter(new LocalTimeToStringConverter());
        conversionService.addConverter(new StringToLocalTimeConverter());
        conversionService.addConverter(new LocalDateTimeToStringConverter());
        conversionService.addConverter(new StringToLocalDateTimeConverter());
        conversionService.addConverter(new BatchJobParameterWrapperToStringConverter());
        conversionService.addConverter(new StringToBatchJobParameterWrapperConverter());
        return conversionService;
    }
}
