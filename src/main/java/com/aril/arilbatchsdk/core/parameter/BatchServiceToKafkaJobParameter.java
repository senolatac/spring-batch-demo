package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.item.service.reader.ServiceItemReader;
import com.aril.valhala.batch.IdempotentBatchItem;
import com.aril.valhala.event.IdempotentJobEvent;
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
public class BatchServiceToKafkaJobParameter extends BaseBatchJobParameter {

    @NonNull
    private String topic;

    private String consumerCompletedNotifierTopic;

    @NonNull
    private transient ServiceItemReader<? extends IdempotentBatchItem> serviceItemReader;

    private IdempotencyOptions idempotencyOptions;

    private transient ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentJobEvent<?>> itemProcessor;
}
