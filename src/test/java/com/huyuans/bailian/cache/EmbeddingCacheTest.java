package com.huyuans.bailian.cache;

import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.model.response.EmbeddingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmbeddingCache.
 */
class EmbeddingCacheTest {

    private EmbeddingCache cache;

    @BeforeEach
    void setUp() {
        BailianProperties.EmbeddingCacheConfig config = new BailianProperties.EmbeddingCacheConfig();
        config.setEnabled(true);
        config.setMaxSize(100);
        config.setExpireMinutes(60);
        cache = new EmbeddingCache(config);
    }

    @Test
    void testGenerateKey() {
        String key = cache.generateKey("text-embedding-v3", Arrays.asList("hello", "world"));
        assertTrue(key.startsWith("emb:"));
    }

    @Test
    void testSameInputSameKey() {
        List<String> texts = Arrays.asList("test");
        assertEquals(cache.generateKey("model", texts), cache.generateKey("model", texts));
    }

    @Test
    void testPutAndGet() {
        EmbeddingResponse response = createTestResponse();
        cache.put("key", response);

        assertTrue(cache.get("key").isPresent());
        assertEquals(response.getId(), cache.get("key").get().getId());
    }

    @Test
    void testCacheMiss() {
        assertFalse(cache.get("non-existent").isPresent());
    }

    @Test
    void testDisabledCache() {
        BailianProperties.EmbeddingCacheConfig config = new BailianProperties.EmbeddingCacheConfig();
        config.setEnabled(false);
        EmbeddingCache disabledCache = new EmbeddingCache(config);

        disabledCache.put("key", createTestResponse());
        assertFalse(disabledCache.get("key").isPresent());
    }

    @Test
    void testClear() {
        cache.put("key1", createTestResponse());
        cache.put("key2", createTestResponse());
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    void testSize() {
        assertEquals(0, cache.size());
        cache.put("key1", createTestResponse());
        assertEquals(1, cache.size());
    }

    private EmbeddingResponse createTestResponse() {
        return EmbeddingResponse.builder()
                .id("test-id-" + System.currentTimeMillis())
                .model("text-embedding-v3")
                .embeddings(Arrays.asList(EmbeddingResponse.Embedding.builder()
                        .embedding(Arrays.asList(0.1f, 0.2f, 0.3f)).index(0).build()))
                .build();
    }
}
