package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.parameter.support.ServiceIdempotentItemWriterParameter;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BatchExcelToServiceJobParameter extends BaseBatchFromCsvOrExcelJobParameter {

    @NonNull
    private transient ServiceIdempotentItemWriterParameter serviceItemWriter;
}
