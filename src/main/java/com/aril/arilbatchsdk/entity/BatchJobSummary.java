package com.aril.arilbatchsdk.entity;

import com.aril.arilbatchsdk.core.BatchType;
import com.aril.valhala.product.ModuleType;
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
@Table(name = "co_batch_job_summaries", indexes = {
        @Index(columnList = "job_name", name = "idx_co_batch_job_summaries_jobname"),
        @Index(columnList = "export_url", name = "idx_co_batch_job_summaries_exporturl")
})
public class BatchJobSummary {

    @Id
    @Column(name = "job_instance_id", nullable = false)
    private Long jobInstanceId;

    @Column(name = "job_owner")
    private String jobOwner;

    //consider as type, group, name
    @Column(name = "job_name", nullable = false)
    private String jobName;

    //name might be used for grouping so to specify it, use label
    @Column(name = "job_label")
    private String jobLabel;

    @Column(name = "read_count", nullable = false)
    private Long readCount;

    @Column(name = "write_count", nullable = false)
    private Long writeCount;

    @Column(name = "skip_count", nullable = false)
    private Long skipCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_type", length = 20)
    private ModuleType module;

    @Enumerated(EnumType.STRING)
    @Column(name = "batch_type", nullable = false, length = 20)
    private BatchType batchType;

    @Column(name = "export_url")
    private String exportUrl;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @Column(name = "end_time")
    private Long endTime;

    @Column(name = "last_updated", nullable = false)
    private Long lastUpdated;

    @JdbcTypeCode(SqlTypes.JSON) //default-format for postgres is jsonb
    @Column(name = "metadata")
    private Map<String, Object> metadata;

    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "summary", optional = false)
    private BatchJobConsumerTracker consumerTracker;

    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "summary", optional = false)
    private BatchJobResultSummary resultSummary;

    @Version
    @Column(name = "version")
    private Long version;
}
