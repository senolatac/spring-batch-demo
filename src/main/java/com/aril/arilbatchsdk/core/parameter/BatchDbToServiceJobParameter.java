package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryItemReaderParameter;
import com.aril.arilbatchsdk.core.parameter.support.ServiceIdempotentItemWriterParameter;
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
public class BatchDbToServiceJobParameter extends BaseBatchJobParameter {

    @NonNull
    private transient RepositoryItemReaderParameter repositoryItemReader;

    @NonNull
    private transient ServiceIdempotentItemWriterParameter serviceItemWriter;

    private IdempotencyOptions idempotencyOptions;

    private transient ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentBatchItem> itemProcessor;
}
