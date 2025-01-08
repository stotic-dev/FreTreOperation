package com.stotic_dev.FreTreOperation;

import com.stotic_dev.FreTreOperation.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(
        classes = {TestConfig.class},
        initializers = {ConfigDataApplicationContextInitializer.class}
)
class BatchApplicationRunnerTest {

    @Test
    void batchEntryTest() {

    }
}