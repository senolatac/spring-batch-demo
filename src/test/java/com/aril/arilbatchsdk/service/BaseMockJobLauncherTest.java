package com.aril.arilbatchsdk.service;

import com.aril.arilbatchsdk.entity.BatchJobConsumerTracker;
import com.aril.arilidempotentsdk.core.ArilIdempotentTemplate;
import com.aril.arilidempotentsdk.core.operation.IdempotentValueOperations;
import com.aril.arilkafka.core.ArilKafkaTemplate;
import com.aril.arilkafka.service.KafkaStatsService;
import com.aril.arilkafka.support.TopicDetail;
import com.aril.arilkafka.support.TopicPartitionDetail;
import com.aril.arilminiosdk.service.MinioService;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public abstract class BaseMockJobLauncherTest extends BaseJobLauncherTest {

    @MockBean
    protected ArilIdempotentTemplate arilIdempotentTemplate;

    @MockBean
    protected ArilKafkaTemplate<String, Object> arilKafkaTemplate;

    @MockBean
    protected KafkaStatsService kafkaStatsService;

    @MockBean
    protected MinioService minioService;

    @Mock
    protected IdempotentValueOperations idempotentValueOperations;

    protected void assertConsumePercentage(Long jobId, int percentage) {
        BatchJobConsumerTracker consumerTracker = arilBatchConsumerTrackerService.getJobConsumerTracker(jobId);

        assertThat(consumerTracker.getConsumePercentage()).isEqualTo(percentage);
    }

    protected void prepareMinio(String id) {
        when(minioService.getDownloadLink(anyString(), any(Path.class))).thenReturn(id);
    }

    protected void verifyKafkaInteraction() throws Exception {
        verify(arilIdempotentTemplate, times(100)).valueOps();
        verify(idempotentValueOperations, times(100)).create(any());

        //because it will call inside callable block.
        verify(arilKafkaTemplate, times(0)).send(eq(TOPIC), any());
    }

    protected void prepareKafkaDetails() throws Exception {
        TopicDetail topicDetail = createTopicDetail();
        when(kafkaStatsService.getTopicDetailsWithoutConsumerGroup(TOPIC)).thenReturn(topicDetail);
        when(kafkaStatsService.getTopicDetails(eq(TOPIC), any())).thenReturn(topicDetail);
        prepareIdempotency();
    }

    protected void prepareIdempotency() {
        when(arilIdempotentTemplate.valueOps()).thenReturn(idempotentValueOperations);
    }

    protected TopicDetail createTopicDetail() {
        return TopicDetail.builder()
                .partitions(List.of(createTopicPartitionDetail(0), createTopicPartitionDetail(1)))
                .build();
    }

    protected TopicPartitionDetail createTopicPartitionDetail(int partition) {
        return TopicPartitionDetail.builder()
                .partition(partition)
                .latestConsumedOffset(15L)
                .latestProducedOffset(50L)
                .build();
    }
}
