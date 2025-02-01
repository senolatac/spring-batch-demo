package com.aril.arilbatchsdk.testadapter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobExecutionListenerService {

    public void statusChanged(JobExecution jobExecution) {
        log.info("JobExecutionListener notified with new status: {}", jobExecution.getStatus());
    }
}
