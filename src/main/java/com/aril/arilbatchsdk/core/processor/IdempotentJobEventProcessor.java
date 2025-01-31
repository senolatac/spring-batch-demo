package com.aril.arilbatchsdk.core.processor;

import com.aril.valhala.batch.IdempotentBatchItem;
import com.aril.valhala.event.IdempotentJobEvent;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

public class IdempotentJobEventProcessor implements ItemProcessor<IdempotentBatchItem, IdempotentJobEvent<?>> {

    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Override
    public IdempotentJobEvent<?> process(IdempotentBatchItem item) throws Exception {
        Long jobId = stepExecution.getJobExecution().getId();
        return new IdempotentJobEvent<>(item, jobId, item.getIdempotentKey());
    }
}
