package com.aril.arilbatchsdk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = ArilBatchConfig.class)
class ArilBatchConfigTests {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
            // if context was successfully loaded, this block will be executed
        });
    }

}
