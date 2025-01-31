package com.aril.arilbatchsdk.adapter.kafka.event;

import com.aril.arilbatchsdk.adapter.kafka.data.BatchConsumerCompletedEventData;
import com.aril.valhala.event.BaseEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BatchConsumerCompletedEvent extends BaseEvent implements Serializable {
    private static final String PUBLISHER = "batch-event-publisher";
    private static final String EVENT = "batch-consumer-completed-event";

    private BatchConsumerCompletedEventData data;

    public BatchConsumerCompletedEvent(BatchConsumerCompletedEventData data) {
        super(EVENT, System.currentTimeMillis(), PUBLISHER);
        this.data = data;
    }
}
