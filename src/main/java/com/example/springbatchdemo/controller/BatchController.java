package com.example.springbatchdemo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("batch")
@RequiredArgsConstructor
public class BatchController {
    private final Job readBooksFromCsvToDbJob;
    private final Job writeBooksFromDbToCsvJob;
    private final JobLauncher jobLauncher;

    @Value("classpath:book-data.csv")
    private Resource customFile;

    @PostMapping("read")
    public ResponseEntity<?> read() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addJobParameter("customFile", customFile, Resource.class)
                .toJobParameters();

        JobExecution execution = jobLauncher.run(readBooksFromCsvToDbJob, jobParameters);
        return ResponseEntity.ok(execution.getStatus());
    }

    @PostMapping("write")
    public ResponseEntity<?> write() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addString("outputPath", "data/output.csv")
                .toJobParameters();

        JobExecution execution = jobLauncher.run(writeBooksFromDbToCsvJob, jobParameters);
        return ResponseEntity.ok(execution.getStatus());
    }
}
