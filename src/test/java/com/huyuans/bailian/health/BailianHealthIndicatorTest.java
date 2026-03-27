package com.huyuans.bailian.health;

import com.huyuans.bailian.config.BailianProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BailianHealthIndicator.
 */
class BailianHealthIndicatorTest {

    private BailianProperties properties;

    @BeforeEach
    void setUp() {
        properties = new BailianProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://dashscope.aliyuncs.com");
    }

    @Test
    void testHealthIndicatorCreation() {
        assertNotNull(new BailianHealthIndicator(properties));
    }

    @Test
    void testHealthBuilderUp() {
        Health health = Health.up()
                .withDetail("baseUrl", "https://test.com")
                .withDetail("status", "API response OK")
                .build();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("https://test.com", health.getDetails().get("baseUrl"));
    }

    @Test
    void testHealthBuilderDown() {
        Health health = Health.down()
                .withDetail("error", "Connection failed")
                .build();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Connection failed", health.getDetails().get("error"));
    }
}
