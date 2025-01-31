package com.aril.arilbatchsdk.core.tasklet.export;

import com.aril.arilbatchsdk.core.FileUploader;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@RequiredArgsConstructor
public class ExportMetadataTasklet implements Tasklet {
    private final FileUploader fileUploader;
    private final BatchJobSummaryService arilBatchJobSummaryService;
    private final String folder;
    private final String fileName;
    private final String bucket;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        saveJobExport(chunkContext.getStepContext().getJobInstanceId());
        return RepeatStatus.FINISHED;
    }

    private void saveJobExport(Long jobInstanceId) {
        arilBatchJobSummaryService.saveExportUrl(jobInstanceId, fileUploader.getDownloadUrl(bucket, folder, fileName));
    }
}
