package com.stotic_dev.FreTreOperation.config;

import com.stotic_dev.FreTreOperation.BatchApplicationRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@TestConfiguration
@ComponentScan(
        basePackages = {"com.stotic_dev.FreTreOperation"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {BatchApplicationRunner.class})
)
public class TestConfig { }
