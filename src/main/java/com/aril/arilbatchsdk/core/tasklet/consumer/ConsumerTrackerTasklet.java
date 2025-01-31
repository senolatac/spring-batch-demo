package com.aril.arilbatchsdk.core.tasklet.consumer;

import com.aril.arilbatchsdk.facade.BatchConsumerTrackerFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@RequiredArgsConstructor
public class ConsumerTrackerTasklet implements Tasklet {
    private final BatchConsumerTrackerFacade batchConsumerTrackerFacade;
    private final String topic;
    private final String notifierTopic;
    private final String chunkStepName;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Long jobId = chunkContext.getStepContext().getJobInstanceId();
        batchConsumerTrackerFacade.createConsumerTracker(jobId, topic, notifierTopic, chunkStepName);
        return RepeatStatus.FINISHED;
    }
}
