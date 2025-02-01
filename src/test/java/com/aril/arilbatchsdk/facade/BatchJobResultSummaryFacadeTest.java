package com.aril.arilbatchsdk.facade;

import com.aril.arilbatchsdk.BaseDbTest;
import com.aril.arilbatchsdk.entity.BatchJobResultSummary;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Sql("/data/sql/batch-job-summary.sql")
@Sql("/data/sql/batch-job-item-result.sql")
@Sql("/data/sql/batch-job-result-summary.sql")
class BatchJobResultSummaryFacadeTest extends BaseDbTest {

    @Autowired
    private BatchJobResultSummaryFacade batchJobResultSummaryFacade;

    private static final Long JOB_ID_NEW = 100L;

    @Test
    void getAll() {
        Page<BatchJobResultSummary> resultSummaries = batchJobResultSummaryFacade.getAll(Pageable.ofSize(3));

        assertThat(resultSummaries.getTotalElements()).isEqualTo(2L);
    }

    @Test
    void summarizeItemResults() {
        batchJobResultSummaryFacade.summarizeItemResults(JOB_ID_NEW);

        BatchJobResultSummary resultSummary = batchJobResultSummaryFacade.getByJobInstanceId(JOB_ID_NEW);

        assertThat(resultSummary)
                .returns(1L, BatchJobResultSummary::getSuccessCount)
                .returns(1L, BatchJobResultSummary::getFailCount);
    }
}