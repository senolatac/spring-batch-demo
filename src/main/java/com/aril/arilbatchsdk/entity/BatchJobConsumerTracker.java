package com.aril.arilbatchsdk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.batch.core.BatchStatus;

import java.util.Map;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_batch_job_consumer_trackers")
public class BatchJobConsumerTracker {

    @Id
    @Column(name = "job_instance_id", nullable = false)
    private Long jobInstanceId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "notifier_topic")
    private String notifierTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchStatus status;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @Column(name = "end_time")
    private Long endTime;

    @Column(name = "last_updated", nullable = false)
    private Long lastUpdated;

    @Column(name = "read_count", nullable = false)
    private Long readCount;

    @Column(name = "produce_count", nullable = false)
    private Long produceCount;

    @Column(name = "produce_skip_count", nullable = false)
    private Long produceSkipCount;

    @Column(name = "consume_percentage")
    private Integer consumePercentage;

    @JdbcTypeCode(SqlTypes.JSON) //default-format for postgres is jsonb
    @Column(name = "produce_snapshot")
    private Map<Integer, Long> produceSnapshot;

    @MapsId
    @JoinColumn(name = "job_instance_id", insertable = false, updatable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private BatchJobSummary summary;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
