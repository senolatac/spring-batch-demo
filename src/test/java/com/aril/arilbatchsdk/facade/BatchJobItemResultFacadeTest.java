package com.aril.arilbatchsdk.facade;

import com.aril.arilbatchsdk.BaseDbTest;
import com.aril.arilbatchsdk.entity.BatchJobItemResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Sql("/data/sql/batch-job-item-result.sql")
class BatchJobItemResultFacadeTest extends BaseDbTest {

    @Autowired
    private BatchJobItemResultFacade batchJobItemResultFacade;

    private static final Long JOB_ID = 100L;

    @Test
    void getJobItemResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BatchJobItemResult> itemResults = batchJobItemResultFacade.getJobItemResults(JOB_ID, pageable);

        assertThat(itemResults).hasSize(2);
    }
}