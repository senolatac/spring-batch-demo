package com.aril.arilbatchsdk.core.listener;

import com.aril.arilbatchsdk.core.BatchType;
import com.aril.arilbatchsdk.core.parameter.BaseBatchJobParameter;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import com.aril.valhala.product.ModuleType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
public class ChunkStepExecutionListener implements StepExecutionListener {
    private final BatchType batchType;
    private final ModuleType moduleType;
    private final BaseBatchJobParameter baseBatchJobParameter;
    private final BatchJobSummaryService arilBatchJobSummaryService;

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        arilBatchJobSummaryService.save(batchType, moduleType, baseBatchJobParameter, stepExecution, stepExecution.getEndTime());
    }

    @Override
    @Nullable
    public ExitStatus afterStep(StepExecution stepExecution) {
        //there is an issue about it: https://github.com/spring-projects/spring-batch/issues/3846
        //so set it anyway in afterStep
        LocalDateTime endTime = Objects.requireNonNullElse(stepExecution.getEndTime(), LocalDateTime.now());
        arilBatchJobSummaryService.save(batchType, moduleType, baseBatchJobParameter, stepExecution, endTime);
        return ExitStatus.COMPLETED;
    }
}
