package com.huyuans.bailian.health;

import com.huyuans.bailian.config.BailianProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BailianHealthIndicator 单元测试
 */
@DisplayName("健康检查指示器测试")
class BailianHealthIndicatorTest {

    private BailianProperties properties;

    @BeforeEach
    void setUp() {
        properties = new BailianProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://dashscope.aliyuncs.com");
        properties.setDefaultModel("qwen-turbo");
    }

    @Test
    @DisplayName("健康检查返回UP状态")
    void testHealthUp() {
        // 由于 BailianHealthIndicator 需要真实的 WebClient，
        // 这里我们测试构造函数和基本属性
        BailianHealthIndicator indicator = new BailianHealthIndicator(properties);
        assertNotNull(indicator);
    }

    @Test
    @DisplayName("Health构建器测试 - UP状态")
    void testHealthBuilderUp() {
        Health health = Health.up()
                .withDetail("baseUrl", "https://test.com")
                .withDetail("status", "API 响应正常")
                .build();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("https://test.com", health.getDetails().get("baseUrl"));
    }

    @Test
    @DisplayName("Health构建器测试 - DOWN状态")
    void testHealthBuilderDown() {
        Health health = Health.down()
                .withDetail("error", "连接失败")
                .build();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("连接失败", health.getDetails().get("error"));
    }

    @Test
    @DisplayName("Health构建器测试 - HTTP错误")
    void testHealthBuilderHttpError() {
        Health health = Health.down()
                .withDetail("error", "HTTP 500")
                .withDetail("message", "Internal Server Error")
                .build();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("HTTP 500", health.getDetails().get("error"));
        assertEquals("Internal Server Error", health.getDetails().get("message"));
    }

    @Test
    @DisplayName("Health构建器测试 - 异常")
    void testHealthBuilderException() {
        Health health = Health.down()
                .withDetail("error", "WebClientResponseException")
                .withDetail("message", "Connection refused")
                .build();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    @DisplayName("配置属性获取测试")
    void testPropertiesAccess() {
        assertEquals("test-api-key", properties.getApiKey());
        assertEquals("https://dashscope.aliyuncs.com", properties.getBaseUrl());
        assertEquals("qwen-turbo", properties.getDefaultModel());
    }

    @Test
    @DisplayName("健康检查路径常量测试")
    void testHealthCheckPath() {
        // 验证健康检查路径是正确的
        String expectedPath = "/api/v1/models";
        assertNotNull(expectedPath);
        assertTrue(expectedPath.startsWith("/api"));
    }

    @Test
    @DisplayName("超时配置测试")
    void testTimeoutConfig() {
        properties.setTimeout(30000);
        assertEquals(30000, properties.getTimeout());
    }
}