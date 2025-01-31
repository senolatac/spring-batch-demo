package com.aril.arilbatchsdk.facade;

import com.aril.arilbatchsdk.adapter.kafka.ArilKafkaProducerAdapter;
import com.aril.arilbatchsdk.adapter.kafka.KafkaConsumerStatsAdapter;
import com.aril.arilbatchsdk.adapter.kafka.data.BatchConsumerCompletedEventData;
import com.aril.arilbatchsdk.adapter.kafka.event.BatchConsumerCompletedEvent;
import com.aril.arilbatchsdk.core.explore.ArilJobExplorer;
import com.aril.arilbatchsdk.entity.BatchJobConsumerTracker;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.service.BatchConsumerTrackerService;
import com.aril.arilbatchsdk.service.BatchJobSummaryService;
import com.aril.arilbatchsdk.util.JobUtils;
import com.aril.arilkafka.core.ArilKafkaTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass({JpaRepository.class, ArilKafkaTemplate.class})
public class BatchConsumerTrackerFacade {
    private final BatchConsumerTrackerService batchConsumerTrackerService;
    private final BatchJobResultSummaryFacade batchJobResultSummaryFacade;
    private final KafkaConsumerStatsAdapter kafkaConsumerStatsAdapter;
    private final BatchJobSummaryService batchJobSummaryService;
    private final ArilJobExplorer jobExplorer;
    private final ArilKafkaProducerAdapter arilKafkaProducerAdapter;

    public void createConsumerTracker(Long jobInstanceId, String topic, String notifierTopic, String stepName) throws Exception {
        StepExecution writerStepExecution = jobExplorer.getLatestStepExecution(jobInstanceId, stepName);
        BatchJobSummary jobSummary = batchJobSummaryService.getById(jobInstanceId);
        Map<Integer, Long> producerSnapshot = kafkaConsumerStatsAdapter.findCurrentProducerSnapshot(topic);

        batchConsumerTrackerService.createConsumerTracker(jobSummary, topic, notifierTopic, writerStepExecution, producerSnapshot);
    }

    //no need to transaction, each item might be executed in seperated session.
    public void updateStatusOfExecutingConsumers() {
        List<BatchJobConsumerTracker> consumerTrackers = batchConsumerTrackerService.getProcessingConsumers();

        for (BatchJobConsumerTracker consumerTracker : consumerTrackers) {
            updateStatusOfConsumerTrackerInSafeMode(consumerTracker);
        }
    }

    public void updateStatusOfConsumerTracker(Long jobInstanceId, String consumerGroupId) throws Exception {
        BatchJobConsumerTracker consumerTracker = batchConsumerTrackerService.getJobConsumerTracker(jobInstanceId);

        Integer consumePercentage = calculateConsumePercentage(consumerTracker, consumerGroupId);
        consumerTracker.setConsumePercentage(consumePercentage);

        batchConsumerTrackerService.updateConsumerTrackerStatus(consumerTracker);

        sendCompletedNotification(consumerTracker);
    }

    private void updateStatusOfConsumerTrackerInSafeMode(BatchJobConsumerTracker consumerTracker) {
        try {
            Integer consumePercentage = calculateConsumePercentage(consumerTracker, null);
            consumerTracker.setConsumePercentage(consumePercentage);
            batchConsumerTrackerService.updateConsumerTrackerStatus(consumerTracker.getJobInstanceId(), consumePercentage);
            sendCompletedNotification(consumerTracker);

            batchJobResultSummaryFacade.summarizeItemResults(consumerTracker.getJobInstanceId());
        } catch (Exception e) {
            log.error("Unexpected error occurred and ignored on status-update-of-consumer", e);
        }
    }

    private Integer calculateConsumePercentage(BatchJobConsumerTracker consumerTracker, String consumerGroupId) throws Exception {
        if (consumerTracker.getProduceCount() == 0L) {
            return JobUtils.HUNDRED_PERCENT;
        }

        Long lag = kafkaConsumerStatsAdapter.calculateCurrentConsumerLag(consumerTracker.getProduceSnapshot(), consumerTracker.getTopic(), consumerGroupId);

        //unexpected case, might be topic-deletion etc.
        if (lag == null) {
            return null;
        }

        double consumePercentage = 100.0 * (consumerTracker.getProduceCount() - lag) / consumerTracker.getProduceCount();

        //it should be between 0 and 100.
        return Math.max(Math.min((int) consumePercentage, JobUtils.HUNDRED_PERCENT), 0);
    }

    private void sendCompletedNotification(BatchJobConsumerTracker consumerTracker) {
        if (consumerTracker.getNotifierTopic() == null || consumerTracker.getConsumePercentage() == null) {
            return;
        }
        if (consumerTracker.getConsumePercentage().compareTo(JobUtils.HUNDRED_PERCENT) == 0) {
            arilKafkaProducerAdapter.publish(consumerTracker.getNotifierTopic(), new BatchConsumerCompletedEvent(convertToEventData(consumerTracker)));
        }
    }

    private BatchConsumerCompletedEventData convertToEventData(BatchJobConsumerTracker consumerTracker) {
        return BatchConsumerCompletedEventData.builder()
                .jobInstanceId(consumerTracker.getJobInstanceId())
                .endTime(consumerTracker.getEndTime())
                .lastUpdated(consumerTracker.getLastUpdated())
                .createTime(consumerTracker.getCreateTime())
                .produceSkipCount(consumerTracker.getProduceSkipCount())
                .readCount(consumerTracker.getReadCount())
                .startTime(consumerTracker.getStartTime())
                .consumePercentage(consumerTracker.getConsumePercentage())
                .produceCount(consumerTracker.getProduceCount())
                .status(consumerTracker.getStatus())
                .build();
    }
}
