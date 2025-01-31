package com.aril.arilbatchsdk.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_batch_job_result_summaries")
public class BatchJobResultSummary {

    @Id
    @Column(name = "job_instance_id", nullable = false, unique = true)
    private Long jobInstanceId;

    @Column(name = "success_count", nullable = false)
    private Long successCount;

    @Column(name = "fail_count", nullable = false)
    private Long failCount;

    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @Column(name = "last_updated", nullable = false)
    private Long lastUpdated;

    @MapsId
    @JoinColumn(name = "job_instance_id", insertable = false, updatable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private BatchJobSummary summary;

    @Version
    @Column(name = "version")
    private Long version;
}
