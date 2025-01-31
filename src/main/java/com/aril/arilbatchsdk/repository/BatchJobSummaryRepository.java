package com.aril.arilbatchsdk.repository;

import com.aril.arilbatchsdk.entity.BatchJobSummary;
import com.aril.valhala.product.ModuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BatchJobSummaryRepository extends JpaRepository<BatchJobSummary, Long>, JpaSpecificationExecutor<BatchJobSummary> {

    Optional<BatchJobSummary> findByJobInstanceId(Long jobInstanceId);

    @Query("""
            select b from BatchJobSummary b
            left join fetch b.consumerTracker
            left join fetch b.resultSummary
            where b.jobInstanceId = :jobInstanceId
            """)
    BatchJobSummary findWithDetails(@Param("jobInstanceId") Long jobInstanceId);

    @Query("""
            select b from BatchJobSummary b
            left join fetch b.consumerTracker
            left join fetch b.resultSummary
            where (:module IS NULL OR b.module = :module)
            AND (:jobName IS NULL OR b.jobName = :jobName)
            """)
    Page<BatchJobSummary> findAllWithDetails(@Param("module") ModuleType module, @Param("jobName") String jobName, Pageable pageable);

    @Query("""
            select b from BatchJobSummary b
            where (:module IS NULL OR b.module = :module)
            AND (:jobName IS NULL OR b.jobName = :jobName)
            """)
    Page<BatchJobSummary> findAllByModuleAndJobName(@Param("module") ModuleType module, @Param("jobName") String jobName, Pageable pageable);

    @Query("Select distinct b.jobName from BatchJobSummary b")
    List<String> findAllJobNames();
}
