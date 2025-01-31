package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.entity.BatchJobConsumerTracker;
import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.arilbatchsdk.repository.BatchJobConsumerTrackerRepository;
import com.aril.arilbatchsdk.util.JobUtils;
import com.aril.valhala.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Lazy
@Service
@RequiredArgsConstructor
@ConditionalOnClass({JpaRepository.class})
public class BatchConsumerTrackerService {
    private final BatchJobConsumerTrackerRepository batchJobConsumerTrackerRepository;

    public BatchJobConsumerTracker getJobConsumerTracker(Long jobId) {
        return batchJobConsumerTrackerRepository.findByJobInstanceId(jobId);
    }

    public void createConsumerTracker(BatchJobSummary batchJobSummary, String topic, String notifierTopic, StepExecution stepExecution, Map<Integer, Long> producerSnapshot) {
        BatchJobConsumerTracker consumerTracker = BatchJobConsumerTracker.builder()
                .jobInstanceId(batchJobSummary.getJobInstanceId())
                .topic(topic)
                .notifierTopic(notifierTopic)
                .createTime(DateUtils.toReportLongFormat(stepExecution.getCreateTime()))
                .startTime(DateUtils.toReportLongFormat(stepExecution.getStartTime()))
                .readCount(stepExecution.getReadCount())
                .status(BatchStatus.STARTED)
                .produceCount(stepExecution.getWriteCount())
                .produceSkipCount(stepExecution.getWriteSkipCount())
                .lastUpdated(DateUtils.toReportLongFormat(LocalDateTime.now()))
                .produceSnapshot(producerSnapshot)
                .consumePercentage(0)
                .summary(batchJobSummary)
                .build();

        batchJobConsumerTrackerRepository.save(consumerTracker);
    }

    //ignore FAILED and COMPLETED states.
    public List<BatchJobConsumerTracker> getProcessingConsumers() {
        return batchJobConsumerTrackerRepository.findByStatus(BatchStatus.STARTED);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateConsumerTrackerStatus(BatchJobConsumerTracker consumerTracker) {
        BatchStatus status = calculateCompleteStatus(consumerTracker.getConsumePercentage());
        consumerTracker.setLastUpdated(DateUtils.toReportLongFormat(LocalDateTime.now()));
        consumerTracker.setStatus(status);

        if (status == BatchStatus.COMPLETED) {
            consumerTracker.setEndTime(Objects.requireNonNullElse(consumerTracker.getEndTime(), DateUtils.toReportLongFormat(LocalDateTime.now())));
        }

        batchJobConsumerTrackerRepository.save(consumerTracker);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateConsumerTrackerStatus(Long jobInstanceId, Integer consumePercentage) {
        BatchJobConsumerTracker consumerTracker = batchJobConsumerTrackerRepository.findByJobInstanceId(jobInstanceId);
        consumerTracker.setConsumePercentage(consumePercentage);
        consumerTracker.setLastUpdated(DateUtils.toReportLongFormat(LocalDateTime.now()));

        BatchStatus status = calculateCompleteStatus(consumerTracker.getConsumePercentage());
        consumerTracker.setStatus(status);

        if (status == BatchStatus.COMPLETED) {
            consumerTracker.setEndTime(Objects.requireNonNullElse(consumerTracker.getEndTime(), DateUtils.toReportLongFormat(LocalDateTime.now())));
        }

        batchJobConsumerTrackerRepository.save(consumerTracker);
    }

    private BatchStatus calculateCompleteStatus(Integer percentage) {
        //null means unexpected case.
        if (percentage == null) {
            return BatchStatus.FAILED;
        } else if (percentage.compareTo(JobUtils.HUNDRED_PERCENT) == 0) {
            return BatchStatus.COMPLETED;
        }
        return BatchStatus.STARTED;
    }
}
