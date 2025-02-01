package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.BaseDbTest;
import com.aril.arilbatchsdk.entity.BatchJobResultSummary;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Sql("/data/sql/batch-job-summary.sql")
@Sql("/data/sql/batch-job-item-result.sql")
@Sql("/data/sql/batch-job-result-summary.sql")
class BatchJobResultSummaryServiceTest extends BaseDbTest {

    @Autowired
    private BatchJobResultSummaryService batchJobResultSummaryService;

    @Autowired
    private BatchJobSummaryService batchJobSummaryService;

    private static final Long JOB_ID_PRE = 200L;
    private static final Long JOB_ID_NEW = 100L;

    @Test
    void findByJobInstanceId() {
        BatchJobResultSummary resultSummary = batchJobResultSummaryService.getByJobInstanceId(JOB_ID_PRE);

        assertThat(resultSummary)
                .returns(10L, BatchJobResultSummary::getSuccessCount)
                .returns(20L, BatchJobResultSummary::getFailCount);
    }

    @Test
    void findAll() {
        Page<BatchJobResultSummary> resultSummaries = batchJobResultSummaryService.findAll(Pageable.ofSize(3));

        assertThat(resultSummaries.getTotalElements()).isEqualTo(2L);
    }

    @Test
    void summarizeItemResults() {
        BatchJobSummary jobSummary = batchJobSummaryService.getById(JOB_ID_NEW);
        Map<BatchStatus, Long> statusMap = Map.of(BatchStatus.FAILED, 1L, BatchStatus.COMPLETED, 1L);

        batchJobResultSummaryService.saveItemResults(jobSummary, JOB_ID_NEW, statusMap);

        BatchJobResultSummary resultSummary = batchJobResultSummaryService.getByJobInstanceId(JOB_ID_NEW);

        assertThat(resultSummary)
                .returns(1L, BatchJobResultSummary::getSuccessCount)
                .returns(1L, BatchJobResultSummary::getFailCount);
    }
}