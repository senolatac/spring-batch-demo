package com.aril.arilbatchsdk.repository.projection;

import org.springframework.batch.core.BatchStatus;

public interface StatusCounterProjection {

    BatchStatus getStatus();

    Long getCount();
}
