package com.aril.arilbatchsdk.core.processor;

import com.aril.valhala.batch.IdempotentBatchItem;
import com.aril.valhala.event.IdempotentJobEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class IdempotentJobEventProcessor implements ItemProcessor<IdempotentBatchItem, IdempotentJobEvent<?>> {
    private final String jobOwner;

    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Override
    public IdempotentJobEvent<?> process(@NonNull IdempotentBatchItem item) throws Exception {
        Long jobId = stepExecution.getJobExecution().getJobId();
        IdempotentJobEvent<IdempotentBatchItem> event = new IdempotentJobEvent<>(item, jobId, item.getIdempotentKey());

        if (jobOwner != null) {
            event.setPublisher(jobOwner);
        }
        return event;
    }
}
