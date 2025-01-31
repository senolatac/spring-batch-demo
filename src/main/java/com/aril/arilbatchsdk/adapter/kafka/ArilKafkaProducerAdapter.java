package com.aril.arilbatchsdk.adapter.kafka;

import com.aril.arilkafka.core.ArilKafkaTemplate;
import com.aril.valhala.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass({ArilKafkaTemplate.class})
public class ArilKafkaProducerAdapter {
    private final ArilKafkaTemplate<String, Object> arilKafkaTemplate;

    public <E extends BaseEvent> void publish(String topic, E event) {
        try {
            arilKafkaTemplate.send(topic, event);
        } catch (Exception ex) {
            log.error("Kafka publish-event exception ignored for topic: {}", topic, ex);
        }
    }
}
