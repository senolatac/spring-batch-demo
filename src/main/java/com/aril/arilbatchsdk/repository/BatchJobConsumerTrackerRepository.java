package com.aril.arilbatchsdk.repository;

import com.aril.arilbatchsdk.entity.BatchJobConsumerTracker;
import org.springframework.batch.core.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchJobConsumerTrackerRepository extends JpaRepository<BatchJobConsumerTracker, Long> {
    BatchJobConsumerTracker findByJobInstanceId(Long jobInstanceId);

    List<BatchJobConsumerTracker> findByStatus(BatchStatus status);
}
