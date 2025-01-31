package com.aril.arilbatchsdk.core.explore;

import com.aril.arilbatchsdk.util.JobUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;

import java.util.List;


@RequiredArgsConstructor
public class ArilJobExplorer {

    private final JobRepository jobRepository;

    public List<JobExecution> getJobExecutions(Long jobId) {
        JobInstance jobInstance = JobUtils.getDefaultJobInstance(jobId);
        return jobRepository.findJobExecutions(jobInstance);
    }

    public StepExecution getLatestStepExecution(Long jobId, String stepName) {
        JobInstance jobInstance = JobUtils.getDefaultJobInstance(jobId);
        return jobRepository.getLastStepExecution(jobInstance, stepName);
    }
}
