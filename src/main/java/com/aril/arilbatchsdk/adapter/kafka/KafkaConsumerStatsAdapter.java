package com.aril.arilbatchsdk.adapter.kafka;

import com.aril.arilkafka.core.ArilKafkaTemplate;
import com.aril.arilkafka.service.KafkaStatsService;
import com.aril.arilkafka.support.TopicDetail;
import com.aril.arilkafka.support.TopicPartitionDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass({ArilKafkaTemplate.class})
public class KafkaConsumerStatsAdapter {
    private final KafkaStatsService kafkaStatsService;

    public Map<Integer, Long> findCurrentProducerSnapshot(String topic) throws Exception {
        TopicDetail topicDetail = kafkaStatsService.getTopicDetailsWithoutConsumerGroup(topic);

        return topicDetail.getPartitions().stream()
                .collect(Collectors.toMap(TopicPartitionDetail::getPartition, TopicPartitionDetail::getLatestProducedOffset));
    }

    public Long calculateCurrentConsumerLag(Map<Integer, Long> produceSnapshot, String topic, String consumerGroupId) throws Exception {
        if (consumerGroupId == null) {
            return calculateCurrentConsumerLag(produceSnapshot, topic);
        }
        TopicDetail topicDetail = kafkaStatsService.getTopicDetails(topic, consumerGroupId);

        if (topicDetail == null) {
            return null;
        }

        return topicDetail.getPartitions().stream()
                .mapToLong(partitionDetail -> calculateLag(produceSnapshot, partitionDetail))
                .sum();
    }

    private Long calculateCurrentConsumerLag(Map<Integer, Long> produceSnapshot, String topic) throws Exception {
        TopicDetail topicDetail = kafkaStatsService.getTopicDetailsWithAllConsumerGroups(topic);

        if (topicDetail == null) {
            return null;
        }

        //if there are multi-consumer-group for single topic, then return max-lag.
        return topicDetail.getPartitions().stream()
                .collect(Collectors.toMap(TopicPartitionDetail::getPartition, partitionDetail -> calculateLag(produceSnapshot, partitionDetail), Math::max))
                .values().stream()
                .mapToLong(lag -> lag)
                .sum();
    }

    private Long calculateLag(Map<Integer, Long> produceSnapshot, TopicPartitionDetail partitionDetail) {
        Long expectedOffset = produceSnapshot.get(partitionDetail.getPartition());
        Long currentOffset = Objects.requireNonNullElse(partitionDetail.getLatestConsumedOffset(), 0L);

        //it means, no active-partition for this calculation so return no-lag.
        if (expectedOffset == null) {
            return 0L;
        }

        //if currentOffset is passed the expected-value, then mark it as completed and don't return as minus.
        return Math.max(0L, expectedOffset - currentOffset);
    }
}
