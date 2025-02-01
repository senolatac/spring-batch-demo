package com.aril.arilbatchsdk.core.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class JobExecutionStatusChangeListener implements JobExecutionListener {
    private final Consumer<JobExecution> jobExecutionChangeSubscriber;

    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        if (jobExecutionChangeSubscriber != null) {
            jobExecutionChangeSubscriber.accept(jobExecution);
        }
    }
}
