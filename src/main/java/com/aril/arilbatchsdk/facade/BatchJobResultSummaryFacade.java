package com.aril.arilbatchsdk.facade;

import com.aril.arilbatchsdk.entity.BatchJobResultSummary;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.service.BatchJobItemResultService;
import com.aril.arilbatchsdk.service.BatchJobResultSummaryService;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnClass({JpaRepository.class})
public class BatchJobResultSummaryFacade {
    private final BatchJobResultSummaryService batchJobResultSummaryService;
    private final BatchJobSummaryService batchJobSummaryService;
    private final BatchJobItemResultService batchJobItemResultService;

    public BatchJobResultSummary getByJobInstanceId(Long jobId) {
        return batchJobResultSummaryService.getByJobInstanceId(jobId);
    }

    public Page<BatchJobResultSummary> getAll(Pageable pageable) {
        return batchJobResultSummaryService.findAll(pageable);
    }

    @Transactional
    public void summarizeItemResults(Long jobId) {
        BatchJobSummary jobSummary = batchJobSummaryService.getById(jobId);
        Map<BatchStatus, Long> statusMap = batchJobItemResultService.countByStatus(jobId);

        batchJobResultSummaryService.saveItemResults(jobSummary, jobId, statusMap);
    }
}
