package com.aril.arilbatchsdk.core.parameter;

import com.aril.valhala.security.ArilSecurityContextHolder;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.core.JobExecution;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseBatchJobParameter implements Serializable {

    /***
     * It is required for concurrency for similar jobs.
     */
    @NonNull
    private String jobName;

    private String jobLabel;

    @Builder.Default
    private String jobOwner = ArilSecurityContextHolder.getContext().getUsername();

    @Builder.Default
    private int chunkSize = 100;

    /***
     * Job metadata like triggered-user...
     */
    @SuppressWarnings("java:S1948")
    private Map<String, Object> metadata;

    /***
     * Listen async job-executions to detect whether completed or not.
     */
    private transient Consumer<JobExecution> jobExecutionChangeSubscriber;
}
