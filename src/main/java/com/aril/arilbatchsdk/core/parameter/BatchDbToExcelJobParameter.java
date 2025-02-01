package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.parameter.support.RepositoryItemReaderParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDbToExcelJobParameter extends BaseBatchToExcelJobParameter {

    @NonNull
    private transient RepositoryItemReaderParameter repositoryItemReader;
}
