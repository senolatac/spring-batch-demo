package com.aril.arilbatchsdk.facade;

import com.aril.arilbatchsdk.adapter.kafka.ArilKafkaProducerAdapter;
import com.aril.arilbatchsdk.adapter.kafka.KafkaConsumerStatsAdapter;
import com.aril.arilbatchsdk.adapter.kafka.event.BatchConsumerCompletedEvent;
import com.aril.arilbatchsdk.core.explore.ArilJobExplorer;
import com.aril.arilbatchsdk.entity.BatchJobConsumerTracker;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.service.BatchConsumerTrackerService;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchConsumerTrackerFacadeTest {

    @InjectMocks
    private BatchConsumerTrackerFacade batchConsumerTrackerFacade;

    @Mock
    private BatchConsumerTrackerService batchConsumerTrackerService;

    @Mock
    private ArilJobExplorer arilJobExplorer;

    @Mock
    private KafkaConsumerStatsAdapter kafkaConsumerStatsAdapter;

    @Mock
    private BatchJobSummaryService batchJobSummaryService;

    @Mock
    private BatchJobResultSummaryFacade batchJobResultSummaryFacade;

    @Mock
    private ArilKafkaProducerAdapter arilKafkaProducerAdapter;

    @Captor
    private ArgumentCaptor<BatchJobConsumerTracker> batchJobConsumerTrackerArgumentCaptor;

    private static final String TOPIC = "test-topic";
    private static final String NOTIFIER_TOPIC = "test-notifier-topic";
    private static final String STEP_NAME = "test-step";
    private static final Long JOB_ID = 1L;
    private static final String CONSUMER_GROUP_1 = "cg1";


    @Test
    void createConsumerTracker() throws Exception {
        StepExecution stepExecution = stepExecution();
        BatchJobSummary jobSummary = batchJobSummary();
        Map<Integer, Long> producerSnapshot = produceSnapshot();
        when(kafkaConsumerStatsAdapter.findCurrentProducerSnapshot(TOPIC)).thenReturn(producerSnapshot);
        when(arilJobExplorer.getLatestStepExecution(JOB_ID, STEP_NAME)).thenReturn(stepExecution);
        when(batchJobSummaryService.getById(JOB_ID)).thenReturn(jobSummary);

        batchConsumerTrackerFacade.createConsumerTracker(JOB_ID, TOPIC, NOTIFIER_TOPIC, STEP_NAME);

        verify(batchConsumerTrackerService).createConsumerTracker(jobSummary, TOPIC, NOTIFIER_TOPIC, stepExecution, producerSnapshot);
    }

    @Test
    void updateStatusOfExecutingConsumers() throws Exception {
        BatchJobConsumerTracker consumerTracker = consumerTracker();
        when(batchConsumerTrackerService.getProcessingConsumers()).thenReturn(List.of(consumerTracker));
        when(kafkaConsumerStatsAdapter.calculateCurrentConsumerLag(anyMap(), eq(TOPIC), any())).thenReturn(60L);

        batchConsumerTrackerFacade.updateStatusOfExecutingConsumers();

        verify(batchJobResultSummaryFacade).summarizeItemResults(JOB_ID);
        verify(batchConsumerTrackerService).updateConsumerTrackerStatus(JOB_ID, 40);
        verifyNoInteractions(arilKafkaProducerAdapter);
    }

    @Test
    void updateStatusOfConsumerTracker() throws Exception {
        BatchJobConsumerTracker consumerTracker = consumerTracker();
        when(batchConsumerTrackerService.getJobConsumerTracker(JOB_ID)).thenReturn(consumerTracker);
        when(kafkaConsumerStatsAdapter.calculateCurrentConsumerLag(anyMap(), eq(TOPIC), eq(CONSUMER_GROUP_1))).thenReturn(60L);

        batchConsumerTrackerFacade.updateStatusOfConsumerTracker(JOB_ID, CONSUMER_GROUP_1);

        verify(batchConsumerTrackerService).updateConsumerTrackerStatus(batchJobConsumerTrackerArgumentCaptor.capture());
        assertThat(batchJobConsumerTrackerArgumentCaptor.getValue())
                .returns(40, BatchJobConsumerTracker::getConsumePercentage)
                .returns(BatchStatus.STARTED, BatchJobConsumerTracker::getStatus)
                .returns(TOPIC, BatchJobConsumerTracker::getTopic);
        verifyNoInteractions(arilKafkaProducerAdapter);
    }

    @Test
    void updateStatusOfConsumerTracker_withCompleted() throws Exception {
        BatchJobConsumerTracker consumerTracker = consumerTracker();
        when(batchConsumerTrackerService.getJobConsumerTracker(JOB_ID)).thenReturn(consumerTracker);
        when(kafkaConsumerStatsAdapter.calculateCurrentConsumerLag(anyMap(), eq(TOPIC), eq(CONSUMER_GROUP_1))).thenReturn(0L);

        batchConsumerTrackerFacade.updateStatusOfConsumerTracker(JOB_ID, CONSUMER_GROUP_1);

        verify(batchConsumerTrackerService).updateConsumerTrackerStatus(batchJobConsumerTrackerArgumentCaptor.capture());
        assertThat(batchJobConsumerTrackerArgumentCaptor.getValue())
                .returns(100, BatchJobConsumerTracker::getConsumePercentage)
                .returns(BatchStatus.STARTED, BatchJobConsumerTracker::getStatus)
                .returns(TOPIC, BatchJobConsumerTracker::getTopic);
        verify(arilKafkaProducerAdapter).publish(eq(NOTIFIER_TOPIC), any(BatchConsumerCompletedEvent.class));
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
                .notifierTopic(NOTIFIER_TOPIC)
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