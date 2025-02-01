package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.item.service.reader.ServiceItemReader;
import com.aril.valhala.batch.BatchItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BatchServiceToExcelJobParameter extends BaseBatchToExcelJobParameter {

    @NonNull
    private transient ServiceItemReader<? extends BatchItem> serviceItemReader;
}
