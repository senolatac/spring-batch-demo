package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.entity.BatchJobConsumerTracker;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.jpa.repository.BatchJobConsumerTrackerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchConsumerTrackerServiceTest {

    @InjectMocks
    private BatchConsumerTrackerService batchConsumerTrackerService;

    @Mock
    private BatchJobConsumerTrackerRepository batchJobConsumerTrackerRepository;

    @Captor
    private ArgumentCaptor<BatchJobConsumerTracker> batchJobConsumerTrackerArgumentCaptor;

    private static final String TOPIC = "test-topic";
    private static final String NOTIFIER_TOPIC = "test-notifier-topic";
    private static final String STEP_NAME = "test-step";
    private static final Long JOB_ID = 1L;

    @Test
    void createConsumerTracker() {
        StepExecution stepExecution = stepExecution();
        BatchJobSummary jobSummary = batchJobSummary();
        Map<Integer, Long> producerSnapshot = produceSnapshot();

        batchConsumerTrackerService.createConsumerTracker(jobSummary, TOPIC, NOTIFIER_TOPIC, stepExecution, producerSnapshot);

        verify(batchJobConsumerTrackerRepository).save(batchJobConsumerTrackerArgumentCaptor.capture());
        assertThat(batchJobConsumerTrackerArgumentCaptor.getValue())
                .returns(0, BatchJobConsumerTracker::getConsumePercentage)
                .returns(BatchStatus.STARTED, BatchJobConsumerTracker::getStatus)
                .returns(TOPIC, BatchJobConsumerTracker::getTopic);
        assertThat(batchJobConsumerTrackerArgumentCaptor.getValue().getProduceSnapshot()).hasSize(3);
    }

    @Test
    void updateConsumerTrackerStatus() {
        BatchJobConsumerTracker consumerTracker = consumerTracker();

        batchConsumerTrackerService.updateConsumerTrackerStatus(consumerTracker);

        verify(batchJobConsumerTrackerRepository).save(batchJobConsumerTrackerArgumentCaptor.capture());
        assertThat(batchJobConsumerTrackerArgumentCaptor.getValue())
                .returns(0, BatchJobConsumerTracker::getConsumePercentage)
                .returns(BatchStatus.STARTED, BatchJobConsumerTracker::getStatus)
                .returns(TOPIC, BatchJobConsumerTracker::getTopic);
    }

    @Test
    void updateConsumerTrackerStatus_withId() {
        BatchJobConsumerTracker consumerTracker = consumerTracker();
        when(batchJobConsumerTrackerRepository.findByJobInstanceId(JOB_ID)).thenReturn(consumerTracker);

        batchConsumerTrackerService.updateConsumerTrackerStatus(JOB_ID, 0);

        verify(batchJobConsumerTrackerRepository).save(batchJobConsumerTrackerArgumentCaptor.capture());
        assertThat(batchJobConsumerTrackerArgumentCaptor.getValue())
                .returns(0, BatchJobConsumerTracker::getConsumePercentage)
                .returns(BatchStatus.STARTED, BatchJobConsumerTracker::getStatus)
                .returns(TOPIC, BatchJobConsumerTracker::getTopic);
    }

    private BatchJobSummary batchJobSummary() {
        return BatchJobSummary.builder()
                .jobInstanceId(JOB_ID)
                .build();
    }

    private StepExecution stepExecution() {
        StepExecution stepExecution = new StepExecution(STEP_NAME, new JobExecution(JOB_ID));
        stepExecution.setWriteSkipCount(10L);
        stepExecution.setReadCount(100L);
        stepExecution.setWriteCount(90L);
        return stepExecution;
    }

    private BatchJobConsumerTracker consumerTracker() {
        return BatchJobConsumerTracker.builder()
                .topic(TOPIC)
                .jobInstanceId(JOB_ID)
                .produceCount(100L)
                .status(BatchStatus.STARTED)
                .produceSnapshot(produceSnapshot())
                .consumePercentage(0)
                .build();
    }

    private Map<Integer, Long> produceSnapshot() {
        return Map.of(0, 20L, 1, 30L, 2, 50L);
    }
}