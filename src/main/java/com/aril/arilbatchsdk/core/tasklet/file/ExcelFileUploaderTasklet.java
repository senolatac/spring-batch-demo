package com.aril.arilbatchsdk.core.tasklet.file;

import com.aril.arilbatchsdk.core.FileUploader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@RequiredArgsConstructor
public class ExcelFileUploaderTasklet implements Tasklet {
    private final FileUploader fileUploader;
    private final String bucket;
    private final String folder;
    private final String fileName;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {
        fileUploader.uploadExcelFile(bucket, folder, fileName);
        return RepeatStatus.FINISHED;
    }
}
