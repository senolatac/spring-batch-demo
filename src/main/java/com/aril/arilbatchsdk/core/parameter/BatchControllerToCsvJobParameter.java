package com.aril.arilbatchsdk.core.parameter;

import com.aril.arilbatchsdk.core.item.controller.ControllerItemReader;
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
public class BatchControllerToCsvJobParameter extends BaseBatchToCsvJobParameter {

    @NonNull
    private transient ControllerItemReader<? extends BatchItem> controllerItemReader;
}
