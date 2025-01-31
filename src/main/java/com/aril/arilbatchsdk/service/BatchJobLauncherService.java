package com.aril.arilbatchsdk.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

@Lazy
@Service
public class BatchJobLauncherService extends BaseBatchJobLauncherService {
    private final JobLauncher jobLauncher;

    protected BatchJobLauncherService(@Autowired(required = false) Map<String, Job> jobs,
                                      JobLauncher jobLauncher) {
        super(jobs);
        this.jobLauncher = jobLauncher;
    }

    @Override
    protected JobLauncher jobLauncher() {
        return jobLauncher;
    }
}
