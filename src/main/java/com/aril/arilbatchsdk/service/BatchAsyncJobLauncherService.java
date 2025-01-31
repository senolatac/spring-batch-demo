package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.config.ArilBatchConfigConstants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;

@Lazy
@Service
public class BatchAsyncJobLauncherService extends BaseBatchJobLauncherService {
    private final JobLauncher asyncJobLauncher;

    protected BatchAsyncJobLauncherService(@Autowired(required = false) Map<String, Job> jobs,
                                           @Qualifier(ArilBatchConfigConstants.ASYNC_JOB_LAUNCHER) JobLauncher asyncJobLauncher) {
        super(jobs);
        this.asyncJobLauncher = asyncJobLauncher;
    }

    @Override
    protected JobLauncher jobLauncher() {
        return asyncJobLauncher;
    }
}
