package com.aril.arilbatchsdk.facade;

import com.aril.arilbatchsdk.ArilBatchConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = ArilBatchConfig.class)
class BatchConsumerTrackerFacadeIntegrationTest {

    @Autowired
    private BatchConsumerTrackerFacade batchConsumerTrackerFacade;

    @Test
    void updateStatusOfExecutingConsumers() {
        batchConsumerTrackerFacade.updateStatusOfExecutingConsumers();

        assertDoesNotThrow(() -> {
            // if context was successfully loaded, this block will be executed
        });
    }
}