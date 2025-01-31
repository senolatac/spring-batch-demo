package com.aril.arilbatchsdk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.batch.core.BatchStatus;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "co_batch_job_item_results", indexes = {
        @Index(columnList = "jobInstanceId,status", name = "idx_co_batch_job_item_results_jobInstanceId_status")
})
public class BatchJobItemResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_instance_id", nullable = false)
    private Long jobInstanceId;

    @Column(name = "idempotent_key", nullable = false)
    private String idempotentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchStatus status;

    @JdbcTypeCode(SqlTypes.JSON) //default-format for postgres is jsonb
    @Column(name = "response")
    private String response;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "resource_value")
    private String resourceValue;

    @Column(name = "result_name")
    private String resultName;

    @Column(name = "result_value")
    private String resultValue;

    @Column(name = "error_detail")
    private String errorDetail;

    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @Column(name = "end_time")
    private Long endTime;

    @Version
    @Column(name = "version")
    private Long version;
}
