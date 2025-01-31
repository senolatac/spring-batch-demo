package com.aril.arilbatchsdk.core.tasklet.file;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class LocalFileDeleteTasklet implements Tasklet {
    private final String fileName;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Files.deleteIfExists(Path.of(fileName));
        return RepeatStatus.FINISHED;
    }
}
