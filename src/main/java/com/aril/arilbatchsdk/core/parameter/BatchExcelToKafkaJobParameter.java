package com.aril.arilbatchsdk.core.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExcelToKafkaJobParameter extends BaseBatchFromCsvOrExcelJobParameter {

    @NonNull
    private String topic;

    private String consumerCompletedNotifierTopic;

}
