package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.parameter.support.ServiceIdempotentItemWriterParameter;
import com.aril.valhala.batch.IdempotentBatchItem;
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
public class BatchExcelToServiceJobParameter extends BaseBatchJobParameter {

    @NonNull
    private String[] inputFields;

    private String inputFilePath;

    private transient InputStream inputFileStream;

    @NonNull
    private transient ServiceIdempotentItemWriterParameter serviceItemWriter;

    private IdempotencyOptions idempotencyOptions;

    private transient ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentBatchItem> itemProcessor;

    @NonNull
    private transient Class<? extends IdempotentBatchItem> inputItemClass;

    public Resource getResource() {
        if (inputFilePath != null) {
            return new FileSystemResource(inputFilePath);
        }
        return new InputStreamResource(inputFileStream);
    }
}
