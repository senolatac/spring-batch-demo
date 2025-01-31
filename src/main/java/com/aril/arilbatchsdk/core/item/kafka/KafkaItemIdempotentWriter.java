package com.aril.arilbatchsdk.core.item.kafka;

import com.aril.arilbatchsdk.core.item.AbstractItemIdempotentWriter;
import com.aril.arilkafka.core.ArilKafkaTemplate;
import com.aril.valhala.event.IdempotentJobEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

@Slf4j
@Setter
public class KafkaItemIdempotentWriter<T extends IdempotentJobEvent<?>> extends AbstractItemIdempotentWriter<T, SendResult<String, T>> implements InitializingBean {

    private String topic;
    private ArilKafkaTemplate<String, T> kafkaTemplate;

    public KafkaItemIdempotentWriter() {
        setName(ClassUtils.getShortName(KafkaItemIdempotentWriter.class));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initIdempotencyOptions();
        if (idempotencyOptions.isEnable()) {
            Assert.notNull(idempotentWriter, "The idempotent-writer must be set");
        }
        Assert.notNull(name, "The name must be set");
        Assert.notNull(topic, "The topic must be set");
        Assert.notNull(kafkaTemplate, "The kafka-template must be set");
    }

    @Override
    protected SendResult<String, T> doWrite(T item) throws Exception {
        return kafkaTemplate.send(topic, item).get();
    }
}
