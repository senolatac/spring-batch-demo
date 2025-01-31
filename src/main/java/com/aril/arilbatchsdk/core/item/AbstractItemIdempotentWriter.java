package com.aril.arilbatchsdk.core.item;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.item.support.IdempotentWriter;
import com.aril.arilidempotentsdk.exception.ArilIdempotentException;
import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.concurrent.Callable;

@Setter
@RequiredArgsConstructor
public abstract class AbstractItemIdempotentWriter<T extends IdempotentBatchItem, C> implements ItemWriter<T> {

    protected String name;
    protected IdempotencyOptions idempotencyOptions;
    protected IdempotentWriter idempotentWriter;
    protected StepExecution stepExecution;

    @BeforeStep
    public void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        Chunk<? extends T>.ChunkIterator chunkIterator = chunk.iterator();
        while (chunkIterator.hasNext()) {
            T item = chunkIterator.next();

            //if not idempotent, then mark as skipped.
            try {
                call(item);
            } catch (ArilIdempotentException ex) {
                chunkIterator.remove(ex);

                //there is a known-issue on spring-batch about skip-count https://github.com/spring-projects/spring-batch/discussions/4507
                this.stepExecution.setWriteSkipCount(this.stepExecution.getWriteSkipCount() + 1);
            }
        }
    }

    protected void call(T item) throws Exception {
        Callable<C> callable = () -> doWrite(item);

        if (idempotencyOptions.isEnable()) {
            idempotentWriter.write(name, item, callable, idempotencyOptions);
        } else {
            callable.call();
        }
    }

    /***
     * Default behaviour for idempotency is enabled.
     */
    protected void initIdempotencyOptions() {
        if (idempotencyOptions == null) {
            idempotencyOptions = IdempotencyOptions.builder()
                    .enable(true)
                    .build();
        }
    }

    @SuppressWarnings("java:S112")
    protected abstract C doWrite(T item) throws Exception;
}
