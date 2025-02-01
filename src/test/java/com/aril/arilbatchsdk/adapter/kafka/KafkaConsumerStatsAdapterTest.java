package com.aril.arilbatchsdk.adapter.kafka;

import com.aril.arilkafka.service.KafkaStatsService;
import com.aril.arilkafka.support.TopicDetail;
import com.aril.arilkafka.support.TopicPartitionDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerStatsAdapterTest {

    @InjectMocks
    private KafkaConsumerStatsAdapter kafkaConsumerStatsAdapter;

    @Mock
    private KafkaStatsService kafkaStatsService;

    private static final String TOPIC = "test-topic";
    private static final String CONSUMER_GROUP_1 = "cg1";
    private static final String CONSUMER_GROUP_2 = "cg2";

    @Test
    void findCurrentProducerSnapshot() throws Exception {
        TopicDetail topicDetail = createTopicDetail_notCompleted_singleConsumerGroup();
        when(kafkaStatsService.getTopicDetailsWithoutConsumerGroup(TOPIC)).thenReturn(topicDetail);

        Map<Integer, Long> producerSnapshots = kafkaConsumerStatsAdapter.findCurrentProducerSnapshot(TOPIC);

        assertThat(producerSnapshots).hasSize(3);
    }

    @Test
    void calculateCurrentConsumerLag_withMultipleConsumerGroups_notCompleted() throws Exception {
        TopicDetail topicDetail = createTopicDetail_notCompleted_multipleConsumerGroups();
        Map<Integer, Long> produceSnapshot = produceSnapshot();
        when(kafkaStatsService.getTopicDetailsWithAllConsumerGroups(TOPIC)).thenReturn(topicDetail);

        Long lag = kafkaConsumerStatsAdapter.calculateCurrentConsumerLag(produceSnapshot, TOPIC, null);

        assertThat(lag).isEqualTo(80L);
    }

    @Test
    void calculateCurrentConsumerLag_withMultipleConsumerGroups_completed() throws Exception {
        TopicDetail topicDetail = createTopicDetail_completed();
        Map<Integer, Long> produceSnapshot = produceSnapshot();
        when(kafkaStatsService.getTopicDetailsWithAllConsumerGroups(TOPIC)).thenReturn(topicDetail);

        Long lag = kafkaConsumerStatsAdapter.calculateCurrentConsumerLag(produceSnapshot, TOPIC, null);

        assertThat(lag).isZero();
    }

    @Test
    void calculateCurrentConsumerLag_withSingleConsumerGroup() throws Exception {
        TopicDetail topicDetail = createTopicDetail_notCompleted_singleConsumerGroup();
        Map<Integer, Long> produceSnapshot = produceSnapshot();
        when(kafkaStatsService.getTopicDetails(TOPIC, CONSUMER_GROUP_1)).thenReturn(topicDetail);

        Long lag = kafkaConsumerStatsAdapter.calculateCurrentConsumerLag(produceSnapshot, TOPIC, CONSUMER_GROUP_1);

        assertThat(lag).isEqualTo(60L);
    }

    private Map<Integer, Long> produceSnapshot() {
        return Map.of(0, 20L, 1, 30L, 2, 50L);
    }

    private TopicDetail createTopicDetail_notCompleted_singleConsumerGroup() {
        return TopicDetail.builder()
                .partitions(List.of(
                        createTopicPartitionDetail(0, CONSUMER_GROUP_1, 25L, 30L),
                        createTopicPartitionDetail(1, CONSUMER_GROUP_1, 15L, 30L),
                        createTopicPartitionDetail(2, CONSUMER_GROUP_1, 5L, 50L)
                ))
                .build();
    }

    private TopicDetail createTopicDetail_notCompleted_multipleConsumerGroups() {
        return TopicDetail.builder()
                .partitions(List.of(
                        createTopicPartitionDetail(0, CONSUMER_GROUP_1, 25L, 30L),
                        createTopicPartitionDetail(1, CONSUMER_GROUP_1, 15L, 30L),
                        createTopicPartitionDetail(2, CONSUMER_GROUP_1, 5L, 50L),
                        createTopicPartitionDetail(0, CONSUMER_GROUP_2, 0L, 30L),
                        createTopicPartitionDetail(1, CONSUMER_GROUP_2, 20L, 30L),
                        createTopicPartitionDetail(2, CONSUMER_GROUP_2, 5L, 50L)
                ))
                .build();
    }

    private TopicDetail createTopicDetail_completed() {
        return TopicDetail.builder()
                .partitions(List.of(
                        createTopicPartitionDetail(0, CONSUMER_GROUP_1, 30L, 30L),
                        createTopicPartitionDetail(1, CONSUMER_GROUP_1, 30L, 30L),
                        createTopicPartitionDetail(2, CONSUMER_GROUP_1, 50L, 50L),
                        createTopicPartitionDetail(0, CONSUMER_GROUP_2, 30L, 30L),
                        createTopicPartitionDetail(1, CONSUMER_GROUP_2, 30L, 30L),
                        createTopicPartitionDetail(2, CONSUMER_GROUP_2, 50L, 50L)
                ))
                .build();
    }

    private TopicPartitionDetail createTopicPartitionDetail(int partition, String consumerGroupId, Long consumed, Long produced) {
        return TopicPartitionDetail.builder()
                .partition(partition)
                .latestConsumedOffset(consumed)
                .latestProducedOffset(produced)
                .consumerGroupId(consumerGroupId)
                .build();
    }
}