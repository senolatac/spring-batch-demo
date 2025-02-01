package com.aril.arilbatchsdk.core.listener;

import com.aril.valhala.batch.IdempotentBatchItem;
import com.aril.valhala.event.IdempotentJobEvent;
import lombok.NonNull;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Value;

public class IdempotentJobEventProcessorListener implements ItemProcessListener<IdempotentBatchItem, IdempotentJobEvent<?>> {

    @Value("#{stepExecution}")
    private StepExecution stepExecution;

    @Override
    public void afterProcess(@NonNull IdempotentBatchItem item, IdempotentJobEvent<?> result) {
        Long jobId = stepExecution.getJobExecution().getJobId();

        if (result != null && result.getJobId() == null) {
            result.setJobId(jobId);
        }
    }
}
