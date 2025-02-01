package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.BaseDbTest;
import com.aril.arilbatchsdk.core.result.BatchJobItemResultParameter;
import com.aril.arilbatchsdk.entity.BatchJobItemResult;
import com.aril.valhala.batch.BatchItemResult;
import com.aril.valhala.constants.message.CommonMessages;
import com.aril.valhala.enums.EnumModule;
import com.aril.valhala.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@Sql("/data/sql/batch-job-item-result.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BatchJobItemResultServiceTest extends BaseDbTest {

    @Autowired
    private BatchJobItemResultService batchJobItemResultService;

    @MockBean
    private MessageSourceService messageSourceService;

    @Mock
    private Callable<ConsumerResult> mockCallable;

    private static final Long JOB_ID = 100L;
    private static final Long JOB_ID_NEW = 101L;
    private static final String IDEMPOTENCY_KEY = "key-1";
    private static final String RESOURCE_NAME = "resource-name-1";
    private static final String RESOURCE_VALUE = "resource-value-1";
    private static final String RESULT_NAME = "result-name-1";
    private static final String RESULT_VALUE = "result-value-1";
    private static final String ERROR_DETAIL = "error-detail-1";
    private static final String UNKNOWN_EXCEPTION = "Unknown exception";
    private static final String CORE_UNEXPECTED_ERROR_KEY = "core.core.unexpected.error";

    @Test
    void findJobItemResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BatchJobItemResult> itemResults = batchJobItemResultService.findJobItemResults(JOB_ID, pageable);

        assertThat(itemResults).hasSize(2);
    }

    @Test
    void countByStatus() {
        Map<BatchStatus, Long> countMap = batchJobItemResultService.countByStatus(JOB_ID);

        assertThat(countMap).hasSize(2)
                .containsEntry(BatchStatus.COMPLETED, 1L)
                .containsEntry(BatchStatus.FAILED, 1L);
    }

    @Test
    void save_success_withCompletedState() {
        ConsumerResult consumerResult = new ConsumerResult(1L, IDEMPOTENCY_KEY);
        Callable<ConsumerResult> callable = () -> consumerResult;
        BatchJobItemResultParameter<ConsumerResult> resultParameter = BatchJobItemResultParameter.<ConsumerResult>builder()
                .callable(callable)
                .jobId(JOB_ID_NEW)
                .idempotentKey(IDEMPOTENCY_KEY)
                .resourceName(RESOURCE_NAME)
                .resourceValue(RESOURCE_VALUE)
                .errorMessage(ERROR_DETAIL)
                .build();

        BatchJobItemResult itemResult = batchJobItemResultService.save(resultParameter);

        assertThat(itemResult)
                .returns(JOB_ID_NEW, BatchJobItemResult::getJobInstanceId)
                .returns(BatchStatus.COMPLETED, BatchJobItemResult::getStatus)
                .returns(RESOURCE_NAME, BatchJobItemResult::getResourceName)
                .returns(RESOURCE_VALUE, BatchJobItemResult::getResourceValue)
                .returns(RESULT_NAME, BatchJobItemResult::getResultName)
                .returns(RESULT_VALUE, BatchJobItemResult::getResultValue);
        assertThat(itemResult.getErrorDetail()).isNull();
        assertThat(itemResult.getEndTime()).isNotNull();
        assertThat(itemResult.getResponse()).isNotBlank();
        log.info("Saved item response is: {}", itemResult.getResponse());
    }

    @Test
    void save_success_withFailedState_withDefaultErrorMessage() {
        Callable<ConsumerResult> callable = () -> {
            throw new RuntimeException("Unknown exception");
        };
        BatchJobItemResultParameter<ConsumerResult> resultParameter = BatchJobItemResultParameter.<ConsumerResult>builder()
                .callable(callable)
                .jobId(JOB_ID_NEW)
                .idempotentKey(IDEMPOTENCY_KEY)
                .resourceName(RESOURCE_NAME)
                .resourceValue(RESOURCE_VALUE)
                .errorMessage(ERROR_DETAIL)
                .build();

        when(messageSourceService.getMessage(ERROR_DETAIL)).thenReturn(ERROR_DETAIL);

        BatchJobItemResult itemResult = batchJobItemResultService.save(resultParameter);

        assertThat(itemResult)
                .returns(JOB_ID_NEW, BatchJobItemResult::getJobInstanceId)
                .returns(ERROR_DETAIL, BatchJobItemResult::getErrorDetail)
                .returns(RESOURCE_NAME, BatchJobItemResult::getResourceName)
                .returns(RESOURCE_VALUE, BatchJobItemResult::getResourceValue)
                .returns(BatchStatus.FAILED, BatchJobItemResult::getStatus);
        assertThat(itemResult.getEndTime()).isNull();
        assertThat(itemResult.getResponse()).isNotBlank();
        log.info("Saved item response is: {}", itemResult.getResponse());
    }

    @Test
    void save_success_withFailedState_withoutDefaultErrorMessage() {
        Callable<ConsumerResult> callable = () -> {
            throw new BusinessException(EnumModule.CORE, CommonMessages.UNEXPECTED_ERROR);
        };
        BatchJobItemResultParameter<ConsumerResult> resultParameter = BatchJobItemResultParameter.<ConsumerResult>builder()
                .callable(callable)
                .jobId(JOB_ID_NEW)
                .idempotentKey(IDEMPOTENCY_KEY)
                .resourceName(RESOURCE_NAME)
                .resourceValue(RESOURCE_VALUE)
                .build();

        when(messageSourceService.getMessage(eq(CORE_UNEXPECTED_ERROR_KEY), any(String[].class))).thenReturn(UNKNOWN_EXCEPTION);

        BatchJobItemResult itemResult = batchJobItemResultService.save(resultParameter);

        assertThat(itemResult)
                .returns(JOB_ID_NEW, BatchJobItemResult::getJobInstanceId)
                .returns(UNKNOWN_EXCEPTION, BatchJobItemResult::getErrorDetail)
                .returns(RESOURCE_NAME, BatchJobItemResult::getResourceName)
                .returns(RESOURCE_VALUE, BatchJobItemResult::getResourceValue)
                .returns(BatchStatus.FAILED, BatchJobItemResult::getStatus);
        assertThat(itemResult.getEndTime()).isNull();
        assertThat(itemResult.getResponse()).isNotBlank();
        log.info("Saved item response is: {}", itemResult.getResponse());
    }

    @Test
    void save_success_withFailedState_withRetry() throws Exception {
        when(mockCallable.call()).thenThrow(new RuntimeException());

        BatchJobItemResultParameter<ConsumerResult> resultParameter = BatchJobItemResultParameter.<ConsumerResult>builder()
                .callable(mockCallable)
                .jobId(JOB_ID_NEW)
                .idempotentKey(IDEMPOTENCY_KEY)
                .resourceName(RESOURCE_NAME)
                .resourceValue(RESOURCE_VALUE)
                .errorMessage(ERROR_DETAIL)
                .retry(3)
                .build();

        BatchJobItemResult itemResult = batchJobItemResultService.save(resultParameter);

        assertThat(itemResult)
                .returns(JOB_ID_NEW, BatchJobItemResult::getJobInstanceId)
                .returns(BatchStatus.FAILED, BatchJobItemResult::getStatus);
        verify(mockCallable, times(3)).call();
        log.info("Saved item response is: {}", itemResult.getResponse());
    }

    @Test
    void save_success_withSkipStore() {
        ConsumerResult consumerResult = new ConsumerResult(1L, IDEMPOTENCY_KEY);
        Callable<ConsumerResult> callable = () -> consumerResult;
        BatchJobItemResultParameter<ConsumerResult> resultParameter = BatchJobItemResultParameter.<ConsumerResult>builder()
                .callable(callable)
                .jobId(JOB_ID_NEW)
                .idempotentKey(IDEMPOTENCY_KEY)
                .saveResult(false)
                .build();

        BatchJobItemResult itemResult = batchJobItemResultService.save(resultParameter);

        Page<BatchJobItemResult> itemResults = batchJobItemResultService.findJobItemResults(JOB_ID_NEW, Pageable.ofSize(3));

        assertThat(itemResult).isNotNull();
        assertThat(itemResults).isEmpty();
    }

    private record ConsumerResult(Long id, String key) implements BatchItemResult {
        @Override
        public String getResultName() {
            return RESULT_NAME;
        }

        @Override
        public String getResultValue() {
            return RESULT_VALUE;
        }
    }
}