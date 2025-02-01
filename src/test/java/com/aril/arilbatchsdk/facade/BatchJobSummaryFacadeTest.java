package com.aril.arilbatchsdk.facade;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

@Slf4j
@Sql("/data/sql/batch-job-summary.sql")
@Sql("/data/sql/batch-job-consumer-tracker.sql")
@Sql("/data/sql/batch-job-item-result.sql")
@Sql("/data/sql/batch-job-result-summary.sql")
class BatchJobSummaryFacadeTest extends BaseDbTest {

    @Autowired
    private BatchJobSummaryFacade batchJobSummaryFacade;

    private static final Long JOB_ID_1 = 100L;
    private static final Long JOB_ID_2 = 200L;
    private static final String JOB_NAME_1 = "test-job-1";
    private static final String JOB_NAME_2 = "test-job-2";
    private static final ModuleType MODULE_TYPE_1 = ModuleType.THOR_HOST;
    private static final ModuleType MODULE_TYPE_2 = ModuleType.THOR_PAYMENT;

    @Test
    void getAllJobNames() {
        List<String> jobNames = batchJobSummaryFacade.getAllJobNames();

        assertThat(jobNames).hasSize(3);
    }

    @Test
    void getById() {
        BatchJobSummary batchJobSummary = batchJobSummaryFacade.getById(JOB_ID_1);

        assertThat(batchJobSummary).isNotNull();
    }

    @Test
    void getWithDetails() {
        BatchJobSummary jobSummary = batchJobSummaryFacade.getWithDetails(JOB_ID_2);

        assertThat(jobSummary.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(jobSummary.getConsumerTracker().getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(jobSummary.getResultSummary().getSuccessCount()).isGreaterThan(1L);
    }

    @Test
    void getAll() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAll(Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(3L);
        assertThat(jobSummaries.getContent()).hasSize(2);
    }

    @Test
    void getAll_withModuleType() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAll(MODULE_TYPE_1, Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
    }

    @Test
    void getAll_withJobName() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAll(JOB_NAME_2, Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
    }

    @Test
    void getAll_withModuleTypeAndJobName() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAll(MODULE_TYPE_2, JOB_NAME_2, Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
    }

    @Test
    void getAllWithDetails() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAllWithDetails(MODULE_TYPE_1, null, Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
        assertThat(jobSummaries.getContent().get(0).getResultSummary()).isNull();
    }

    @Test
    void getAll_withFilterSpecification() {
        FilterBatchJobUseCase useCase = FilterBatchJobUseCase.builder()
                .module(MODULE_TYPE_2)
                .jobName(JOB_NAME_2)
                .build();

        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAll(useCase, Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void getAllSummariesOfActiveModule() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAllSummariesOfActiveModule(Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
        //it will give error, because no entity for result-summary and lazy-join
        Exception exception = catchException(() -> jobSummaries.getContent().get(0).getResultSummary().getSuccessCount());
        assertThat(exception).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getAllSummariesOfActiveModuleWithDetails() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAllSummariesOfActiveModuleWithDetails(Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
        assertThat(jobSummaries.getContent().get(0).getResultSummary()).isNull();
    }

    @Test
    void getAllSummariesOfActiveModule_withJobName() {
        Page<BatchJobSummary> jobSummaries = batchJobSummaryFacade.getAllSummariesOfActiveModule(JOB_NAME_1, Pageable.ofSize(2));

        assertThat(jobSummaries.getTotalElements()).isEqualTo(1L);
        assertThat(jobSummaries.getContent()).hasSize(1);
    }
}