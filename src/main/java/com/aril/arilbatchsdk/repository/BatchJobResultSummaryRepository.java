package com.aril.arilbatchsdk.repository;

import com.aril.arilbatchsdk.entity.BatchJobResultSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJobResultSummaryRepository extends JpaRepository<BatchJobResultSummary, Long> {

    BatchJobResultSummary findByJobInstanceId(Long jobInstanceId);
}
