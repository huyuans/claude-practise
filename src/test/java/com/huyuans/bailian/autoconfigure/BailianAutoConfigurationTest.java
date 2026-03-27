package com.huyuans.bailian.autoconfigure;

import com.huyuans.bailian.cache.EmbeddingCache;
import com.huyuans.bailian.client.BailianClient;
import com.huyuans.bailian.config.BailianProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BailianAutoConfiguration.
 */
class BailianAutoConfigurationTest {

    @Test
    void testEmbeddingCacheCreation() {
        BailianProperties.EmbeddingCacheConfig config = new BailianProperties.EmbeddingCacheConfig();
        config.setEnabled(true);
        config.setMaxSize(100);

        EmbeddingCache cache = new EmbeddingCache(config);
        assertNotNull(cache);
        assertEquals(0, cache.size());
    }

    @Test
    void testBailianClientCreation() {
        BailianProperties properties = new BailianProperties();
        properties.setApiKey("test-key");
        properties.getRetry().setEnabled(false);
        properties.getConnectionPool().setEnabled(false);

        assertNotNull(new BailianClient(properties));
    }
}
