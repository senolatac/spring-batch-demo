package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.entity.BatchJobResultSummary;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.jpa.repository.BatchJobResultSummaryRepository;
import com.aril.valhala.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@ConditionalOnClass({JpaRepository.class})
public class BatchJobResultSummaryService {

    private final BatchJobResultSummaryRepository batchJobResultSummaryRepository;

    public BatchJobResultSummary getByJobInstanceId(Long jobId) {
        return batchJobResultSummaryRepository.findByJobInstanceId(jobId);
    }

    public Page<BatchJobResultSummary> findAll(Pageable pageable) {
        return batchJobResultSummaryRepository.findAll(pageable);
    }

    public void saveItemResults(BatchJobSummary batchJobSummary, Long jobId, Map<BatchStatus, Long> statusMap) {
        BatchJobResultSummary resultSummary = getByJobInstanceId(jobId);

        if (resultSummary == null) {
            resultSummary = createSummary(jobId);
        }

        resultSummary.setSummary(batchJobSummary);
        resultSummary.setSuccessCount(Objects.requireNonNullElse(statusMap.get(BatchStatus.COMPLETED), 0L));
        resultSummary.setFailCount(Objects.requireNonNullElse(statusMap.get(BatchStatus.FAILED), 0L));

        batchJobResultSummaryRepository.save(resultSummary);
    }

    private BatchJobResultSummary createSummary(Long jobId) {
        return BatchJobResultSummary.builder()
                .jobInstanceId(jobId)
                .createTime(DateUtils.toReportLongFormat(LocalDateTime.now()))
                .lastUpdated(DateUtils.toReportLongFormat(LocalDateTime.now()))
                .build();
    }
}
