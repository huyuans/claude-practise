package com.huyuans.bailian.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BailianProperties 单元测试
 */
@DisplayName("配置属性测试")
class BailianPropertiesTest {

    @Test
    @DisplayName("默认配置值")
    void testDefaultValues() {
        BailianProperties properties = new BailianProperties();
        
        assertEquals("${DASHSCOPE_API_KEY:}", properties.getApiKey());
        assertEquals("https://dashscope.aliyuncs.com", properties.getBaseUrl());
        assertEquals(60000, properties.getTimeout());
        assertEquals("qwen-turbo", properties.getDefaultModel());
        assertEquals("text-embedding-v3", properties.getDefaultEmbeddingModel());
    }

    @Test
    @DisplayName("设置配置值")
    void testSetValues() {
        BailianProperties properties = new BailianProperties();
        
        properties.setApiKey("my-api-key");
        properties.setBaseUrl("https://custom.url");
        properties.setTimeout(30000);
        properties.setDefaultModel("qwen-max");
        properties.setDefaultEmbeddingModel("text-embedding-v2");
        
        assertEquals("my-api-key", properties.getApiKey());
        assertEquals("https://custom.url", properties.getBaseUrl());
        assertEquals(30000, properties.getTimeout());
        assertEquals("qwen-max", properties.getDefaultModel());
        assertEquals("text-embedding-v2", properties.getDefaultEmbeddingModel());
    }

    @Test
    @DisplayName("重试配置默认值")
    void testRetryConfigDefaults() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.RetryConfig retry = properties.getRetry();
        
        assertTrue(retry.isEnabled());
        assertEquals(3, retry.getMaxAttempts());
        assertEquals(1000, retry.getInitialDelay());
        assertEquals(10000, retry.getMaxDelay());
        assertEquals(2.0, retry.getMultiplier());
    }

    @Test
    @DisplayName("缓存配置默认值")
    void testCacheConfigDefaults() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.EmbeddingCacheConfig cache = properties.getEmbeddingCache();
        
        assertFalse(cache.isEnabled());
        assertEquals(1000, cache.getMaxSize());
        assertEquals(60, cache.getExpireMinutes());
    }

    @Test
    @DisplayName("连接池配置默认值")
    void testConnectionPoolDefaults() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.ConnectionPoolConfig pool = properties.getConnectionPool();
        
        assertTrue(pool.isEnabled());
        assertEquals(100, pool.getMaxConnections());
        assertEquals(50, pool.getMaxConnectionsPerHost());
        assertEquals(20000, pool.getIdleTimeout());
        assertEquals(10000, pool.getAcquireTimeout());
    }

    @Test
    @DisplayName("重试配置修改")
    void testRetryConfigModification() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.RetryConfig retry = properties.getRetry();
        
        retry.setEnabled(false);
        retry.setMaxAttempts(5);
        retry.setInitialDelay(2000);
        retry.setMaxDelay(20000);
        retry.setMultiplier(3.0);
        
        assertFalse(retry.isEnabled());
        assertEquals(5, retry.getMaxAttempts());
        assertEquals(2000, retry.getInitialDelay());
        assertEquals(20000, retry.getMaxDelay());
        assertEquals(3.0, retry.getMultiplier());
    }

    @Test
    @DisplayName("缓存配置修改")
    void testCacheConfigModification() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.EmbeddingCacheConfig cache = properties.getEmbeddingCache();
        
        cache.setEnabled(true);
        cache.setMaxSize(500);
        cache.setExpireMinutes(30);
        
        assertTrue(cache.isEnabled());
        assertEquals(500, cache.getMaxSize());
        assertEquals(30, cache.getExpireMinutes());
    }

    @Test
    @DisplayName("连接池配置修改")
    void testConnectionPoolModification() {
        BailianProperties properties = new BailianProperties();
        BailianProperties.ConnectionPoolConfig pool = properties.getConnectionPool();
        
        pool.setEnabled(false);
        pool.setMaxConnections(50);
        pool.setMaxConnectionsPerHost(25);
        pool.setIdleTimeout(10000);
        pool.setAcquireTimeout(5000);
        
        assertFalse(pool.isEnabled());
        assertEquals(50, pool.getMaxConnections());
        assertEquals(25, pool.getMaxConnectionsPerHost());
        assertEquals(10000, pool.getIdleTimeout());
        assertEquals(5000, pool.getAcquireTimeout());
    }
}