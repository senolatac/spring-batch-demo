package com.aril.arilbatchsdk.config.job;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.item.kafka.KafkaItemIdempotentWriter;
import com.aril.arilbatchsdk.core.item.kafka.KafkaItemIdempotentWriterBuilder;
import com.aril.arilbatchsdk.core.item.support.IdempotentWriter;
import com.aril.arilidempotentsdk.core.ArilIdempotentTemplate;
import com.aril.arilkafka.core.ArilKafkaTemplate;
import com.aril.valhala.event.IdempotentJobEvent;

public abstract class BaseArilBatchKafkaConfig extends BaseArilBatchConfig {

    protected <D extends IdempotentJobEvent<?>> KafkaItemIdempotentWriter<D> configureKafkaItemIdempotentWriter(String jobName,
                                                                                                                String topic,
                                                                                                                IdempotencyOptions idempotencyOptions,
                                                                                                                ArilIdempotentTemplate arilIdempotentTemplate,
                                                                                                                ArilKafkaTemplate<String, D> arilKafkaTemplate) {
        return new KafkaItemIdempotentWriterBuilder<D>()
                .name(jobName)
                .topic(topic)
                .idempotencyOptions(idempotencyOptions)
                .idempotentWriter(new IdempotentWriter(arilIdempotentTemplate))
                .kafkaTemplate(arilKafkaTemplate)
                .build();
    }
}
