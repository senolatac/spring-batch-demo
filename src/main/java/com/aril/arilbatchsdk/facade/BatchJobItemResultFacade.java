package com.aril.arilbatchsdk.facade;

import com.aril.arilbatchsdk.entity.BatchJobItemResult;
import com.aril.arilbatchsdk.service.BatchJobItemResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnClass({JpaRepository.class})
public class BatchJobItemResultFacade {
    private final BatchJobItemResultService batchJobItemResultService;

    public Page<BatchJobItemResult> getJobItemResults(Long jobId, Pageable pageable) {
        return batchJobItemResultService.findJobItemResults(jobId, pageable);
    }
}
