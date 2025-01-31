package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.core.result.BatchJobItemErrorDetail;
import com.aril.arilbatchsdk.core.result.BatchJobItemResultParameter;
import com.aril.arilbatchsdk.entity.BatchJobItemResult;
import com.aril.arilbatchsdk.repository.BatchJobItemResultRepository;
import com.aril.arilbatchsdk.repository.projection.StatusCounterProjection;
import com.aril.arilbatchsdk.util.GsonUtils;
import com.aril.valhala.batch.BatchItemResult;
import com.aril.valhala.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass({JpaRepository.class})
public class BatchJobItemResultService {

    private final BatchJobItemResultRepository batchJobItemResultRepository;
    private final MessageSourceService messageSourceService;

    public Page<BatchJobItemResult> findJobItemResults(Long jobId, Pageable pageable) {
        return batchJobItemResultRepository.findByJobInstanceId(jobId, pageable);
    }

    public Map<BatchStatus, Long> countByStatus(Long jobId) {
        return batchJobItemResultRepository.countByStatus(jobId).stream()
                .collect(Collectors.toMap(StatusCounterProjection::getStatus, StatusCounterProjection::getCount));
    }

    public <T extends BatchItemResult> BatchJobItemResult save(BatchJobItemResultParameter<T> resultParameter) {
        BatchJobItemResult result = execute(resultParameter);

        if (resultParameter.isSaveResult() && result != null) {
            batchJobItemResultRepository.save(result);
        }
        return result;
    }

    private <T extends BatchItemResult> BatchJobItemResult execute(BatchJobItemResultParameter<T> resultParameter) {
        int currentRetry = 0;
        while (currentRetry++ < resultParameter.getRetry()) {
            try {
                T response = resultParameter.getCallable().call();

                return buildCompletedResult(response, resultParameter);
            } catch (Exception ex) {
                log.error("Exception occurred on job-item-result-execution", ex);

                if (currentRetry == resultParameter.getRetry()) {
                    return buildFailedResult(ex, resultParameter);
                }
            }
        }
        return null;
    }

    private <T extends BatchItemResult> BatchJobItemResult buildCompletedResult(T response, BatchJobItemResultParameter<T> resultParameter) {
        return BatchJobItemResult.builder()
                .jobInstanceId(resultParameter.getJobId())
                .idempotentKey(resultParameter.getIdempotentKey())
                .response(GsonUtils.GSON.toJson(response))
                .status(BatchStatus.COMPLETED)
                .createTime(DateUtils.toReportLongFormat(LocalDateTime.now()))
                .endTime(DateUtils.toReportLongFormat(LocalDateTime.now()))
                .resourceName(resultParameter.getResourceName())
                .resourceValue(resultParameter.getResourceValue())
                .resultName(response.getResultName())
                .resultValue(response.getResultValue())
                .build();
    }

    private <T extends BatchItemResult> BatchJobItemResult buildFailedResult(Exception ex, BatchJobItemResultParameter<T> resultParameter) {
        return BatchJobItemResult.builder()
                .jobInstanceId(resultParameter.getJobId())
                .idempotentKey(resultParameter.getIdempotentKey())
                .response(GsonUtils.GSON.toJson(buildExceptionResponse(ex)))
                .status(BatchStatus.FAILED)
                .createTime(DateUtils.toReportLongFormat(LocalDateTime.now()))
                .errorDetail(errorDetail(ex, resultParameter))
                .resourceName(resultParameter.getResourceName())
                .resourceValue(resultParameter.getResourceValue())
                .build();
    }

    private String buildExceptionResponse(Exception ex) {
        BatchJobItemErrorDetail errorDetail = BatchJobItemErrorDetail.builder()
                .exceptionClass(ex.getClass().getCanonicalName())
                .message(ex.getMessage())
                .build();
        return GsonUtils.GSON.toJson(errorDetail);
    }

    private <T extends Serializable> String errorDetail(Exception ex, BatchJobItemResultParameter<T> resultParameter) {
        String error = ex.getMessage();
        if (StringUtils.hasText(resultParameter.getErrorMessage())) {
            error = resultParameter.getErrorMessage();
        }
        return messageSourceService.getMessage(error);
    }
}
