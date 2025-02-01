package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.BaseDbTest;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.jpa.specification.usecase.FilterBatchJobUseCase;
import com.aril.valhala.product.ModuleType;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@Sql("/data/sql/batch-job-summary.sql")
@Sql("/data/sql/batch-job-consumer-tracker.sql")
@Sql("/data/sql/batch-job-result-summary.sql")
class BatchJobSummaryServiceTest extends BaseDbTest {

    @Autowired
    private BatchJobSummaryService batchJobSummaryService;

    private static final Long JOB_ID_1 = 100L;
    private static final Long JOB_ID_2 = 200L;
    private static final String JOB_OWNER = "test-owner";
    private static final String JOB_NAME = "test-job-1";

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void getById() {
        BatchJobSummary jobSummary = batchJobSummaryService.getById(JOB_ID_1);

        assertThat(jobSummary)
                .returns(BatchStatus.COMPLETED, BatchJobSummary::getStatus)
                .returns(JOB_ID_1, BatchJobSummary::getJobInstanceId);
        assertThat(jobSummary.getMetadata()).containsEntry("user", "test-user");
        assertThat(jobSummary.getResultSummary().getJobInstanceId()).isEqualTo(JOB_ID_1);
        assertThat(jobSummary.getConsumerTracker().getJobInstanceId()).isEqualTo(JOB_ID_1);

        //it will give error, because no entity for result-summary.
        Exception exception = catchException(() -> jobSummary.getResultSummary().getSuccessCount());
        assertThat(exception).isInstanceOf(EntityNotFoundException.class);

        //there is consumer-entity related with job-id, so no error. it will fetch it in transaction.
        assertThat(jobSummary.getConsumerTracker().getTopic()).isNotBlank();
    }

    @Test
    void getById_withUnknownJobId() {
        assertThrows(RuntimeException.class, () -> batchJobSummaryService.getById(90L));
    }

    @Test
    void getWithDetails_jobId1() {
        BatchJobSummary jobSummary = batchJobSummaryService.getWithDetails(JOB_ID_1);

        assertThat(jobSummary.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobSummary.getConsumerTracker().getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobSummary.getResultSummary()).isNull();
    }

    @Test
    void getWithDetails_jobId2() {
        BatchJobSummary jobSummary = batchJobSummaryService.getWithDetails(JOB_ID_2);

        assertThat(jobSummary.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(jobSummary.getConsumerTracker().getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(jobSummary.getResultSummary().getSuccessCount()).isGreaterThan(1L);
    }

    @Test
    void getAll() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryService.getAll(Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(3L);
        assertThat(jobSummaries.getContent()).hasSize(2);
    }

    @Test
    void getAllWithDetails() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryService.getAllWithDetails(ModuleType.THOR_HOST, Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
    }

    @Test
    void test_get_all_with_use_case_filter() {

        Long startTime = 20240101101010L;
        Long endTime = 20240101101010L;

        FilterBatchJobUseCase filterBatchJobUseCase = FilterBatchJobUseCase.builder()
                .jobName(JOB_NAME)
                .jobOwner(JOB_OWNER)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        Page<BatchJobSummary> jobSummaries = batchJobSummaryService.getAll(filterBatchJobUseCase, Pageable.ofSize(1));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
    }
}