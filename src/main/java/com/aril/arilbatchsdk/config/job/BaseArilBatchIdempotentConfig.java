package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.item.data.RepositoryItemIdempotentWriter;
import com.aril.arilbatchsdk.core.item.data.RepositoryItemIdempotentWriterBuilder;
import com.aril.arilbatchsdk.core.item.service.IdempotentService;
import com.aril.arilbatchsdk.core.item.service.writer.ServiceItemIdempotentWriter;
import com.aril.arilbatchsdk.core.item.service.writer.ServiceItemIdempotentWriterBuilder;
import com.aril.arilbatchsdk.core.item.support.IdempotentWriter;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryIdempotentItemWriterParameter;
import com.aril.arilbatchsdk.core.parameter.support.ServiceIdempotentItemWriterParameter;
import com.aril.arilidempotentsdk.core.ArilIdempotentTemplate;
import com.aril.valhala.batch.IdempotentBatchItem;
import org.springframework.data.repository.CrudRepository;

public abstract class BaseArilBatchIdempotentConfig extends BaseArilBatchConfig {

    @SuppressWarnings("unchecked")
    protected <D extends IdempotentBatchItem> RepositoryItemIdempotentWriter<D> configureRepositoryItemIdempotentWriter(
            String jobName,
            RepositoryIdempotentItemWriterParameter repositoryItemWriter,
            IdempotencyOptions idempotencyOptions,
            ArilIdempotentTemplate arilIdempotentTemplate) {
        return new RepositoryItemIdempotentWriterBuilder<D>()
                .name(jobName)
                .repository((CrudRepository<D, ?>) repositoryItemWriter.getRepository())
                .methodName(repositoryItemWriter.getMethodName())
                .arguments(repositoryItemWriter.getArguments())
                .idempotencyOptions(idempotencyOptions)
                .idempotentWriter(new IdempotentWriter(arilIdempotentTemplate))
                .build();
    }

    @SuppressWarnings("unchecked")
    protected <D extends IdempotentBatchItem> ServiceItemIdempotentWriter<D> configureServiceItemIdempotentWriter(
            String jobName,
            ServiceIdempotentItemWriterParameter serviceItemWriter,
            IdempotencyOptions idempotencyOptions,
            ArilIdempotentTemplate arilIdempotentTemplate) {
        return new ServiceItemIdempotentWriterBuilder<D>()
                .name(jobName)
                .service((IdempotentService<D>) serviceItemWriter.getService())
                .methodName(serviceItemWriter.getMethodName())
                .arguments(serviceItemWriter.getArguments())
                .idempotencyOptions(idempotencyOptions)
                .idempotentWriter(new IdempotentWriter(arilIdempotentTemplate))
                .build();
    }
}
