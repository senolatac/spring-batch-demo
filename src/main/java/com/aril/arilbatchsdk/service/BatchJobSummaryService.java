package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.parameter.BaseBatchJobParameter;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.jpa.specification.specs.BatchJobSummarySpecification;
import com.aril.arilbatchsdk.jpa.specification.usecase.FilterBatchJobUseCase;
import com.aril.arilbatchsdk.jpa.repository.BatchJobSummaryRepository;
import com.aril.valhala.product.ModuleType;
import com.aril.valhala.security.ArilSecurityContextHolder;
import com.aril.valhala.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnClass({JpaRepository.class})
public class BatchJobSummaryService {
    private final BatchJobSummaryRepository batchJobSummaryRepository;

    public List<String> getAllJobNames() {
        return batchJobSummaryRepository.findAllJobNames();
    }

    public BatchJobSummary getById(Long jobId) {
        return batchJobSummaryRepository.findByJobInstanceId(jobId).orElseThrow();
    }

    public BatchJobSummary getWithDetails(Long jobId) {
        return batchJobSummaryRepository.findWithDetails(jobId);
    }

    public Page<BatchJobSummary> getAll(Pageable pageable) {
        return batchJobSummaryRepository.findAll(pageable);
    }

    public Page<BatchJobSummary> getAllWithDetails(ModuleType moduleType, Pageable pageable) {
        return batchJobSummaryRepository.findAllWithDetails(moduleType, null, pageable);
    }

    public Page<BatchJobSummary> getAllWithDetails(ModuleType moduleType, String jobName, Pageable pageable) {
        return batchJobSummaryRepository.findAllWithDetails(moduleType, jobName, pageable);
    }

    public Page<BatchJobSummary> getAll(ModuleType moduleType, Pageable pageable) {
        return batchJobSummaryRepository.findAllByModuleAndJobName(moduleType, null, pageable);
    }

    public Page<BatchJobSummary> getAll(String jobName, Pageable pageable) {
        return batchJobSummaryRepository.findAllByModuleAndJobName(null, jobName, pageable);
    }

    public Page<BatchJobSummary> getAll(ModuleType moduleType, String jobName, Pageable pageable) {
        return batchJobSummaryRepository.findAllByModuleAndJobName(moduleType, jobName, pageable);
    }

    public void save(BatchType batchType, ModuleType moduleType, BaseBatchJobParameter baseBatchJobParameter, StepExecution stepExecution, LocalDateTime endTime) {
        Long jobInstanceId = stepExecution.getJobExecution().getJobId();
        BatchJobSummary batchJobSummary = batchJobSummaryRepository.findByJobInstanceId(jobInstanceId)
                .orElse(new BatchJobSummary());

        setJobSummaryDetails(batchType, moduleType, batchJobSummary, baseBatchJobParameter, stepExecution, endTime);
        batchJobSummaryRepository.save(batchJobSummary);
    }

    public void saveExportUrl(Long jobId, String url) {
        BatchJobSummary batchJobSummary = getById(jobId);
        batchJobSummary.setExportUrl(url);
        batchJobSummary.setLastUpdated(DateUtils.toReportLongFormat(LocalDateTime.now()));
        batchJobSummaryRepository.save(batchJobSummary);
    }

    private void setJobSummaryDetails(BatchType batchType, ModuleType moduleType, BatchJobSummary batchJobSummary, BaseBatchJobParameter baseBatchJobParameter, StepExecution stepExecution, LocalDateTime endTime) {
        batchJobSummary.setBatchType(batchType);
        batchJobSummary.setModule(moduleType);
        batchJobSummary.setJobLabel(baseBatchJobParameter.getJobLabel());
        batchJobSummary.setJobOwner(baseBatchJobParameter.getJobOwner() != null ? baseBatchJobParameter.getJobOwner() : ArilSecurityContextHolder.getContext().getUsername());
        batchJobSummary.setJobName(baseBatchJobParameter.getJobName());
        batchJobSummary.setJobInstanceId(stepExecution.getJobExecution().getJobId());
        batchJobSummary.setStartTime(DateUtils.toReportLongFormat(stepExecution.getStartTime()));
        batchJobSummary.setCreateTime(DateUtils.toReportLongFormat(stepExecution.getCreateTime()));
        batchJobSummary.setLastUpdated(DateUtils.toReportLongFormat(stepExecution.getLastUpdated()));
        batchJobSummary.setEndTime(DateUtils.toReportLongFormat(endTime));
        batchJobSummary.setStatus(stepExecution.getStatus());
        batchJobSummary.setSkipCount(stepExecution.getSkipCount());
        batchJobSummary.setReadCount(stepExecution.getReadCount());
        batchJobSummary.setWriteCount(stepExecution.getWriteCount());
        batchJobSummary.setMetadata(baseBatchJobParameter.getMetadata());
    }

    public Page<BatchJobSummary> getAll(FilterBatchJobUseCase filterBatchJobUseCase, @NonNull Pageable pageable) {
        Specification<BatchJobSummary> specification = new BatchJobSummarySpecification(filterBatchJobUseCase);

        return batchJobSummaryRepository.findAll(specification, pageable);
    }
}
