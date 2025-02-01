package com.aril.arilbatchsdk;

import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@AutoConfigureDataRedis
@Import(TestConfig.class)
@ContextConfiguration(classes = {ArilBatchConfig.class})
public abstract class BaseDbTest {
}
