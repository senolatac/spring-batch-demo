package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.parameter.support.RepositoryIdempotentItemWriterParameter;
import com.aril.valhala.batch.IdempotentBatchItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExcelToDbJobParameter extends BaseBatchJobParameter {

    @NonNull
    private String[] inputFields;

    private String inputFilePath;

    private transient InputStream inputFileStream;

    private IdempotencyOptions idempotencyOptions;

    private transient ItemProcessor<? extends IdempotentBatchItem, ? extends IdempotentBatchItem> itemProcessor;

    @NonNull
    private transient RepositoryIdempotentItemWriterParameter repositoryItemWriter;

    @NonNull
    private transient Class<? extends IdempotentBatchItem> itemInputClass;

    public Resource getResource() {
        if (inputFilePath != null) {
            return new FileSystemResource(inputFilePath);
        }
        return new InputStreamResource(inputFileStream);
    }
}
