package com.aril.arilbatchsdk.core.item.kafka;

import com.aril.arilbatchsdk.core.IdempotencyOptions;
import com.aril.arilbatchsdk.core.item.support.IdempotentWriter;
import com.aril.arilkafka.core.ArilKafkaTemplate;
import com.aril.valhala.event.IdempotentJobEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaItemIdempotentWriterBuilder<T extends IdempotentJobEvent<?>> {

    private String name = "kafkaItemWriter";
    private String topic;
    private IdempotencyOptions idempotencyOptions = IdempotencyOptions.builder().build();
    private ArilKafkaTemplate<String, T> kafkaTemplate;
    private IdempotentWriter idempotentWriter;

    /**
     * The name used to calculate the key within the
     *
     * @param name name of the writer instance
     * @return The current instance of the builder.
     */
    public KafkaItemIdempotentWriterBuilder<T> name(String name) {
        this.name = name;

        return this;
    }

    /**
     * @param topic kafka-topic-name.
     * @return The current instance of the builder.
     * @see KafkaItemIdempotentWriter#setTopic  (String)
     */
    public KafkaItemIdempotentWriterBuilder<T> topic(String topic) {
        this.topic = topic;

        return this;
    }

    /**
     * idempotency in reusable steps and exceptional cases.
     *
     * @param idempotencyOptions item-idempotency-options
     * @return The current instance of the builder.
     * @see KafkaItemIdempotentWriter#setIdempotencyOptions(IdempotencyOptions)
     */
    public KafkaItemIdempotentWriterBuilder<T> idempotencyOptions(IdempotencyOptions idempotencyOptions) {
        this.idempotencyOptions = idempotencyOptions;

        return this;
    }

    /**
     * @param idempotentWriter idempotent-writer
     * @return The current instance of the builder.
     * @see KafkaItemIdempotentWriter#setIdempotentWriter(IdempotentWriter)
     */
    public KafkaItemIdempotentWriterBuilder<T> idempotentWriter(IdempotentWriter idempotentWriter) {
        this.idempotentWriter = idempotentWriter;

        return this;
    }

    /**
     * @param arilKafkaTemplate kafka-template
     * @return The current instance of the builder.
     * @see KafkaItemIdempotentWriter#setKafkaTemplate(ArilKafkaTemplate)
     */
    public KafkaItemIdempotentWriterBuilder<T> kafkaTemplate(ArilKafkaTemplate<String, T> arilKafkaTemplate) {
        this.kafkaTemplate = arilKafkaTemplate;

        return this;
    }

    /**
     * Validates and builds a {@link KafkaItemIdempotentWriter}.
     *
     * @return a {@link KafkaItemIdempotentWriter}
     */
    public KafkaItemIdempotentWriter<T> build() {
        KafkaItemIdempotentWriter<T> kafkaItemIdempotentWriter = new KafkaItemIdempotentWriter<>();
        kafkaItemIdempotentWriter.setName(name);
        kafkaItemIdempotentWriter.setTopic(topic);
        kafkaItemIdempotentWriter.setIdempotencyOptions(idempotencyOptions);
        kafkaItemIdempotentWriter.setKafkaTemplate(kafkaTemplate);
        kafkaItemIdempotentWriter.setIdempotentWriter(idempotentWriter);

        return kafkaItemIdempotentWriter;
    }
}
