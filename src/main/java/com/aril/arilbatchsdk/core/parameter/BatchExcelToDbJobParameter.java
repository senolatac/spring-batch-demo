package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.parameter.support.RepositoryIdempotentItemWriterParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExcelToDbJobParameter extends BaseBatchFromCsvOrExcelJobParameter {

    @NonNull
    private transient RepositoryIdempotentItemWriterParameter repositoryItemWriter;
}
