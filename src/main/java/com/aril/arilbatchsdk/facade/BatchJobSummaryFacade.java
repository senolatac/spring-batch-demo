package com.aril.arilbatchsdk.facade;

import com.aril.arilbatchsdk.config.ArilBatchProperties;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.jpa.specification.usecase.FilterBatchJobUseCase;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import com.aril.valhala.product.ModuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnClass({JpaRepository.class})
public class BatchJobSummaryFacade {
    private final ArilBatchProperties arilBatchProperties;
    private final BatchJobSummaryService batchJobSummaryService;

    public List<String> getAllJobNames() {
        return batchJobSummaryService.getAllJobNames();
    }

    public BatchJobSummary getById(Long jobId) {
        return batchJobSummaryService.getById(jobId);
    }

    public BatchJobSummary getWithDetails(Long jobId) {
        return batchJobSummaryService.getWithDetails(jobId);
    }

    public Page<BatchJobSummary> getAll(@NonNull Pageable pageable) {
        return batchJobSummaryService.getAll(pageable);
    }

    public Page<BatchJobSummary> getAll(@NonNull ModuleType moduleType, @NonNull Pageable pageable) {
        return batchJobSummaryService.getAll(moduleType, pageable);
    }

    public Page<BatchJobSummary> getAll(@Nullable String jobName, @NonNull Pageable pageable) {
        return batchJobSummaryService.getAll(jobName, pageable);
    }

    public Page<BatchJobSummary> getAll(@Nullable ModuleType moduleType, @Nullable String jobName, @NonNull Pageable pageable) {
        return batchJobSummaryService.getAll(moduleType, jobName, pageable);
    }

    public Page<BatchJobSummary> getAllWithDetails(@Nullable ModuleType moduleType, @Nullable String jobName, @NonNull Pageable pageable) {
        return batchJobSummaryService.getAllWithDetails(moduleType, jobName, pageable);
    }

    public Page<BatchJobSummary> getAllWithDetails(@Nullable String jobName, @NonNull Pageable pageable) {
        return batchJobSummaryService.getAllWithDetails(arilBatchProperties.getActiveModule(), jobName, pageable);
    }

    public Page<BatchJobSummary> getAllSummariesOfActiveModuleWithDetails(@NonNull Pageable pageable) {
        return batchJobSummaryService.getAllWithDetails(arilBatchProperties.getActiveModule(), pageable);
    }

    public Page<BatchJobSummary> getAllSummariesOfActiveModule(@NonNull Pageable pageable) {
        return batchJobSummaryService.getAll(arilBatchProperties.getActiveModule(), pageable);
    }

    public Page<BatchJobSummary> getAllSummariesOfActiveModule(@NonNull String jobName, @NonNull Pageable pageable) {
        return batchJobSummaryService.getAll(arilBatchProperties.getActiveModule(), jobName, pageable);
    }

    public Page<BatchJobSummary> getAll(@NonNull FilterBatchJobUseCase useCase, @NonNull Pageable pageable) {
        useCase.setModule(useCase.getModule() == null ? arilBatchProperties.getActiveModule() : useCase.getModule());
        return batchJobSummaryService.getAll(useCase, pageable);
    }
}
