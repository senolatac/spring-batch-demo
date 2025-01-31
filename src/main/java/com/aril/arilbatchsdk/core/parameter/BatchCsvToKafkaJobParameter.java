package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.valhala.batch.IdempotentBatchItem;
import com.aril.valhala.event.IdempotentJobEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;

@Getter
@SuperBuilder
public class BatchCsvToKafkaJobParameter extends BaseBatchJobParameter {

    @NonNull
    private String topic;

    private String consumerCompletedNotifierTopic;

    @NonNull
    private String[] inputFields;

    @NonNull
    private transient Class<? extends IdempotentBatchItem> inputItemClass;

    private String inputFilePath;

    private transient InputStream inputFileStream;

    private transient ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentJobEvent<?>> itemProcessor;

    private IdempotencyOptions idempotencyOptions;

    public Resource getResource() {
        if (inputFilePath != null) {
            return new FileSystemResource(inputFilePath);
        }
        return new InputStreamResource(inputFileStream);
    }
}
