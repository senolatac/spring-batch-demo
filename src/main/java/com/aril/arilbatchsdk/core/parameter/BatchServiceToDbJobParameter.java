package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.item.service.reader.ServiceItemReader;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryIdempotentItemWriterParameter;
import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.item.ItemProcessor;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BatchServiceToDbJobParameter extends BaseBatchJobParameter {

    private IdempotencyOptions idempotencyOptions;

    @NonNull
    private transient ServiceItemReader<? extends IdempotentBatchItem> serviceItemReader;

    private transient ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentBatchItem> itemProcessor;

    @NonNull
    private transient RepositoryIdempotentItemWriterParameter repositoryItemWriter;
}
