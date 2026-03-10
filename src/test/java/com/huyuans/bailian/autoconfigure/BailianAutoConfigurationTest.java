package com.huyuans.bailian.autoconfigure;

import com.huyuans.bailian.cache.EmbeddingCache;
import com.huyuans.bailian.client.BailianClient;
import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.service.BailianService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BailianAutoConfiguration 单元测试
 */
@DisplayName("自动配置测试")
class BailianAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BailianAutoConfiguration.class));

    @Test
    @DisplayName("属性类测试")
    void testPropertiesClass() {
        BailianProperties properties = new BailianProperties();
        assertNotNull(properties);
        assertEquals("qwen-turbo", properties.getDefaultModel());
    }

    @Test
    @DisplayName("缓存配置测试")
    void testEmbeddingCacheConfig() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.EmbeddingCacheConfig cacheConfig = properties.getEmbeddingCache();
        
        assertNotNull(cacheConfig);
        assertFalse(cacheConfig.isEnabled());
        assertEquals(1000, cacheConfig.getMaxSize());
        assertEquals(60, cacheConfig.getExpireMinutes());
    }

    @Test
    @DisplayName("重试配置测试")
    void testRetryConfig() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.RetryConfig retryConfig = properties.getRetry();
        
        assertNotNull(retryConfig);
        assertTrue(retryConfig.isEnabled());
        assertEquals(3, retryConfig.getMaxAttempts());
        assertEquals(1000, retryConfig.getInitialDelay());
        assertEquals(10000, retryConfig.getMaxDelay());
        assertEquals(2.0, retryConfig.getMultiplier());
    }

    @Test
    @DisplayName("连接池配置测试")
    void testConnectionPoolConfig() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.ConnectionPoolConfig poolConfig = properties.getConnectionPool();
        
        assertNotNull(poolConfig);
        assertTrue(poolConfig.isEnabled());
        assertEquals(100, poolConfig.getMaxConnections());
        assertEquals(50, poolConfig.getMaxConnectionsPerHost());
        assertEquals(20000, poolConfig.getIdleTimeout());
        assertEquals(10000, poolConfig.getAcquireTimeout());
    }

    @Test
    @DisplayName("EmbeddingCache创建测试")
    void testEmbeddingCacheCreation() {
        BailianProperties.EmbeddingCacheConfig config = new BailianProperties.EmbeddingCacheConfig();
        config.setEnabled(true);
        config.setMaxSize(100);
        config.setExpireMinutes(30);
        
        EmbeddingCache cache = new EmbeddingCache(config);
        assertNotNull(cache);
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("BailianClient创建测试")
    void testBailianClientCreation() {
        BailianProperties properties = new BailianProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("https://dashscope.aliyuncs.com");
        
        BailianClient client = new BailianClient(properties);
        assertNotNull(client);
    }

    @Test
    @DisplayName("配置值修改测试")
    void testPropertyModification() {
        BailianProperties properties = new BailianProperties();
        
        properties.setApiKey("new-api-key");
        properties.setBaseUrl("https://new.url");
        properties.setDefaultModel("qwen-max");
        properties.setDefaultEmbeddingModel("text-embedding-v2");
        properties.setTimeout(45000);
        
        assertEquals("new-api-key", properties.getApiKey());
        assertEquals("https://new.url", properties.getBaseUrl());
        assertEquals("qwen-max", properties.getDefaultModel());
        assertEquals("text-embedding-v2", properties.getDefaultEmbeddingModel());
        assertEquals(45000, properties.getTimeout());
    }
}