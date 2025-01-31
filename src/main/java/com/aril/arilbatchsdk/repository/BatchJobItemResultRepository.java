package com.aril.arilbatchsdk.repository;

import com.aril.arilbatchsdk.entity.BatchJobItemResult;
import com.aril.arilbatchsdk.repository.projection.StatusCounterProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BatchJobItemResultRepository extends JpaRepository<BatchJobItemResult, Long> {
    Page<BatchJobItemResult> findByJobInstanceId(Long jobInstanceId, Pageable pageable);

    @Query("""
            select p.status as status, count(p) as count from BatchJobItemResult p
            where p.jobInstanceId = :jobInstanceId
            group by p.status
            """)
    List<StatusCounterProjection> countByStatus(@Param("jobInstanceId") Long jobInstanceId);
}
