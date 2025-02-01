package com.aril.arilbatchsdk.core.parameter;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BatchCsvToKafkaJobParameter extends BaseBatchFromCsvOrExcelJobParameter {

    @NonNull
    private String topic;

    private String consumerCompletedNotifierTopic;
}
