package com.aril.arilbatchsdk.jpa.projection;

import org.springframework.batch.core.BatchStatus;

public interface StatusCounterProjection {

    BatchStatus getStatus();

    Long getCount();
}
