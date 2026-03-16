package com.huyuans.bailian.cache;

import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.model.response.EmbeddingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EmbeddingCache 单元测试
 * <p>
 * 测试 Embedding 缓存的核心功能，包括：
 * <ul>
 *   <li>缓存 key 生成和一致性</li>
 *   <li>缓存的存取和过期</li>
 *   <li>缓存禁用时的行为</li>
 *   <li>缓存淘汰策略</li>
 * </ul>
 *
 * @author Kasper
 * @since 1.0.0
 */
@DisplayName("Embedding缓存测试")
class EmbeddingCacheTest {

    /** 待测试的缓存实例 */
    private EmbeddingCache cache;
    
    /** 缓存配置 */
    private BailianProperties.EmbeddingCacheConfig config;

    @BeforeEach
    void setUp() {
        config = new BailianProperties.EmbeddingCacheConfig();
        config.setEnabled(true);
        config.setMaxSize(100);
        config.setExpireMinutes(60);
        cache = new EmbeddingCache(config);
    }

    @Test
    @DisplayName("生成缓存key")
    void testGenerateKey() {
        List<String> texts = Arrays.asList("hello", "world");
        String key = cache.generateKey("text-embedding-v3", texts);
        assertNotNull(key);
        assertTrue(key.startsWith("emb:"));
    }

    @Test
    @DisplayName("相同输入生成相同key")
    void testSameInputSameKey() {
        List<String> texts = Arrays.asList("test");
        String key1 = cache.generateKey("model", texts);
        String key2 = cache.generateKey("model", texts);
        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("不同输入生成不同key")
    void testDifferentInputDifferentKey() {
        List<String> texts1 = Arrays.asList("hello");
        List<String> texts2 = Arrays.asList("world");
        String key1 = cache.generateKey("model", texts1);
        String key2 = cache.generateKey("model", texts2);
        assertNotEquals(key1, key2);
    }

    @Test
    @DisplayName("存入和获取缓存")
    void testPutAndGet() {
        String key = "test-key";
        EmbeddingResponse response = createTestResponse();
        
        cache.put(key, response);
        Optional<EmbeddingResponse> result = cache.get(key);
        
        assertTrue(result.isPresent());
        assertEquals(response.getId(), result.get().getId());
    }

    @Test
    @DisplayName("缓存未命中返回空")
    void testCacheMiss() {
        Optional<EmbeddingResponse> result = cache.get("non-existent-key");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("禁用缓存时不存储")
    void testDisabledCache() {
        config.setEnabled(false);
        EmbeddingCache disabledCache = new EmbeddingCache(config);
        
        disabledCache.put("key", createTestResponse());
        Optional<EmbeddingResponse> result = disabledCache.get("key");
        
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("禁用缓存时不读取")
    void testDisabledCacheNoRead() {
        config.setEnabled(true);
        cache.put("key", createTestResponse());
        config.setEnabled(false);
        
        EmbeddingCache disabledCache = new EmbeddingCache(config);
        Optional<EmbeddingResponse> result = disabledCache.get("key");
        
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("清除缓存")
    void testClear() {
        cache.put("key1", createTestResponse());
        cache.put("key2", createTestResponse());
        
        cache.clear();
        
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("缓存大小")
    void testSize() {
        assertEquals(0, cache.size());
        
        cache.put("key1", createTestResponse());
        assertEquals(1, cache.size());
        
        cache.put("key2", createTestResponse());
        assertEquals(2, cache.size());
    }

    @Test
    @DisplayName("超过最大容量时清理")
    void testEviction() {
        config.setMaxSize(5);
        EmbeddingCache smallCache = new EmbeddingCache(config);
        
        for (int i = 0; i < 10; i++) {
            smallCache.put("key" + i, createTestResponse());
        }
        
        // 超过maxSize时清除一半
        assertTrue(smallCache.size() <= 5);
    }

    private EmbeddingResponse createTestResponse() {
        EmbeddingResponse response = new EmbeddingResponse();
        response.setId("test-id-" + System.currentTimeMillis());
        response.setModel("text-embedding-v3");
        response.setEmbeddings(List.of(
                EmbeddingResponse.Embedding.builder()
                        .embedding(Arrays.asList(0.1f, 0.2f, 0.3f))
                        .index(0)
                        .build()
        ));
        return response;
    }
}