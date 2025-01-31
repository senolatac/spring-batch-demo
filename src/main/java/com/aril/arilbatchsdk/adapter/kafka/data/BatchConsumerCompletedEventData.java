package com.aril.arilbatchsdk.adapter.kafka.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.BatchStatus;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchConsumerCompletedEventData implements Serializable {
    private Long jobInstanceId;

    private BatchStatus status;

    private Long startTime;

    private Long createTime;

    private Long endTime;

    private Long lastUpdated;

    private Long readCount;

    private Long produceCount;

    private Long produceSkipCount;

    private Integer consumePercentage;
}
