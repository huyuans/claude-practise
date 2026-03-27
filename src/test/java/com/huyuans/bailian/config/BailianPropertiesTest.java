package com.huyuans.bailian.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BailianProperties.
 */
class BailianPropertiesTest {

    @Test
    void testDefaultValues() {
        BailianProperties props = new BailianProperties();

        assertEquals("${DASHSCOPE_API_KEY:}", props.getApiKey());
        assertEquals("https://dashscope.aliyuncs.com", props.getBaseUrl());
        assertEquals(60000, props.getTimeout());
        assertEquals("qwen-turbo", props.getDefaultModel());
        assertEquals("text-embedding-v3", props.getDefaultEmbeddingModel());
    }

    @Test
    void testRetryConfigDefaults() {
        BailianProperties.RetryConfig retry = new BailianProperties().getRetry();

        assertTrue(retry.isEnabled());
        assertEquals(3, retry.getMaxAttempts());
        assertEquals(1000, retry.getInitialDelay());
        assertEquals(10000, retry.getMaxDelay());
        assertEquals(2.0, retry.getMultiplier());
    }

    @Test
    void testCacheConfigDefaults() {
        BailianProperties.EmbeddingCacheConfig cache = new BailianProperties().getEmbeddingCache();

        assertFalse(cache.isEnabled());
        assertEquals(1000, cache.getMaxSize());
        assertEquals(60, cache.getExpireMinutes());
    }

    @Test
    void testConnectionPoolDefaults() {
        BailianProperties.ConnectionPoolConfig pool = new BailianProperties().getConnectionPool();

        assertTrue(pool.isEnabled());
        assertEquals(100, pool.getMaxConnections());
        assertEquals(50, pool.getMaxConnectionsPerHost());
    }

    @Test
    void testPropertyModification() {
        BailianProperties props = new BailianProperties();

        props.setApiKey("new-key");
        props.setBaseUrl("https://custom.url");
        props.setDefaultModel("qwen-max");
        props.setTimeout(30000);

        assertEquals("new-key", props.getApiKey());
        assertEquals("https://custom.url", props.getBaseUrl());
        assertEquals("qwen-max", props.getDefaultModel());
        assertEquals(30000, props.getTimeout());
    }
}
