package com.huyuans.bailian.cache;

import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.model.response.EmbeddingResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Embedding 缓存实现
 * <p>
 * 使用简单的内存缓存，支持 TTL 过期和最大条目数限制。
 * 适用于减少重复文本的 Embedding API 调用，降低成本和延迟。
 * <p>
 * 缓存策略说明：
 * <ul>
 *   <li>缓存 Key：基于模型名称和文本内容生成哈希值</li>
 *   <li>过期策略：基于写入时间戳的 TTL 过期</li>
 *   <li>淘汰策略：当缓存满时，简单清除一半条目（非严格 LRU）</li>
 * </ul>
 * <p>
 * 注意：这是轻量级的内存实现，适用于开发测试环境。
 * 生产环境建议使用 Caffeine（本地缓存）或 Redis（分布式缓存）。
 * <p>
 * 使用示例：
 * <pre>
 * // 通过 BailianService 自动使用缓存
 * EmbeddingResponse response = bailianService.embedding("你好").block();
 * 
 * // 直接使用缓存
 * EmbeddingCache cache = new EmbeddingCache(config);
 * String key = cache.generateKey("text-embedding-v3", List.of("你好"));
 * cache.put(key, response);
 * Optional&lt;EmbeddingResponse&gt; cached = cache.get(key);
 * </pre>
 *
 * @author Kasper
 * @since 1.0.0
 */
@Slf4j
public class EmbeddingCache {

    /** 缓存存储 */
    private final ConcurrentHashMap<String, CacheEntry> cache;
    
    /** 缓存配置 */
    private final BailianProperties.EmbeddingCacheConfig config;
    
    /** 过期时间（毫秒） */
    private final long expireMillis;

    public EmbeddingCache(BailianProperties.EmbeddingCacheConfig config) {
        this.config = config;
        this.cache = new ConcurrentHashMap<>();
        this.expireMillis = config.getExpireMinutes() * 60 * 1000L;
    }

    /**
     * 生成缓存key
     *
     * @param model 模型名称
     * @param texts 文本列表
     * @return 缓存key
     */
    public String generateKey(String model, List<String> texts) {
        int hash = (model + ":" + texts.toString()).hashCode();
        return "emb:" + Integer.toHexString(hash);
    }

    /**
     * 获取缓存的Embedding
     *
     * @param key 缓存key
     * @return Embedding响应（如果存在且未过期）
     */
    public Optional<EmbeddingResponse> get(String key) {
        if (!config.isEnabled()) {
            return Optional.empty();
        }

        CacheEntry entry = cache.get(key);
        if (entry == null) {
            log.debug("缓存未命中: {}", key);
            return Optional.empty();
        }

        if (System.currentTimeMillis() - entry.timestamp > expireMillis) {
            log.debug("缓存已过期: {}", key);
            cache.remove(key);
            return Optional.empty();
        }

        log.debug("缓存命中: {}", key);
        return Optional.of(entry.response);
    }

    /**
     * 存入Embedding到缓存
     * <p>
     * 当缓存达到上限时，会清除一半的旧条目（简单的近似LRU策略）
     * 生产环境建议使用Caffeine等成熟缓存框架
     *
     * @param key      缓存key
     * @param response Embedding响应
     */
    public void put(String key, EmbeddingResponse response) {
        if (!config.isEnabled()) {
            return;
        }

        // 简单的LRU：如果超过最大条目数，清除一半旧条目
        // 注意：这不是真正的LRU，只是简单的清理策略
        if (cache.size() >= config.getMaxSize()) {
            log.debug("缓存已满，清除部分条目");
            cache.keySet().stream()
                    .limit(cache.size() / 2)
                    .forEach(cache::remove);
        }

        cache.put(key, new CacheEntry(response, System.currentTimeMillis()));
        log.debug("存入缓存: {}", key);
    }

    /**
     * 清除所有缓存
     */
    public void clear() {
        cache.clear();
        log.info("缓存已清空");
    }

    /**
     * 获取缓存大小
     */
    public int size() {
        return cache.size();
    }

    private record CacheEntry(EmbeddingResponse response, long timestamp) {}
}