package com.aril.arilbatchsdk.core.item.support;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilidempotentsdk.core.ArilIdempotentTemplate;
import com.aril.arilidempotentsdk.core.param.IdempotentCallableRequestParams;
import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class IdempotentWriter {
    private final ArilIdempotentTemplate idempotentTemplate;

    public <T extends IdempotentBatchItem, C> void write(String jobName, T item, Callable<C> callable, IdempotencyOptions idempotencyOptions) throws Exception {
        IdempotentCallableRequestParams<T, C> callableParam = IdempotentCallableRequestParams.<T, C>builder()
                .jobName(jobName)
                .idempotentKey(item.getIdempotentKey())
                .callable(callable)
                .ttl(idempotencyOptions.getTtl())
                .ttlTimeUnit(idempotencyOptions.getTtlTimeUnit())
                .build();

        idempotentTemplate.valueOps().create(callableParam);
    }
}
