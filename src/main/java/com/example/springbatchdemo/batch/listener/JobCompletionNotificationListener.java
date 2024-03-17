package com.example.springbatchdemo.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    public void afterJob(JobExecution jobExecution) {
        log.info("Job execution state: {}", jobExecution.getStatus());
    }
}
