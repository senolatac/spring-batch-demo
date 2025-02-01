package com.aril.arilbatchsdk.testadapter.consumer;

import com.aril.arilbatchsdk.testadapter.entity.BookEntity;
import com.aril.arilkafka.config.BeanIds;
import com.aril.arilkafka.support.utils.ConsumerConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class BookMessageQueueHandler {

    @KafkaListener(topics = "test-topic",
            containerFactory = BeanIds.ARIL_KAFKA_LISTENER_CONTAINER_FACTORY,
            groupId = ConsumerConfigUtils.DEFAULT_CONSUMER_GROUP)
    public void handle(BookEntity entity) {
        log.info("Message received with id: {}", entity.getId());
    }
}
