package com.huyuans.bailian.cache;

import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.model.response.EmbeddingResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
































@Slf4j
public class EmbeddingCache {

    
    private final ConcurrentHashMap<String, CacheEntry> cache;
    
    
    private final BailianProperties.EmbeddingCacheConfig config;
    
    
    private final long expireMillis;

    public EmbeddingCache(BailianProperties.EmbeddingCacheConfig config) {
        this.config = config;
        this.cache = new ConcurrentHashMap<>();
        this.expireMillis = config.getExpireMinutes() * 60 * 1000L;
    }

    






    public String generateKey(String model, List<String> texts) {
        int hash = (model + ":" + texts.toString()).hashCode();
        return "emb:" + Integer.toHexString(hash);
    }

    





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

    








    public void put(String key, EmbeddingResponse response) {
        if (!config.isEnabled()) {
            return;
        }

        
        
        if (cache.size() >= config.getMaxSize()) {
            log.debug("缓存已满，清除部分条目");
            cache.keySet().stream()
                    .limit(cache.size() / 2)
                    .forEach(cache::remove);
        }

        cache.put(key, new CacheEntry(response, System.currentTimeMillis()));
        log.debug("存入缓存: {}", key);
    }

    


    public void clear() {
        cache.clear();
        log.info("缓存已清空");
    }

    


    public int size() {
        return cache.size();
    }

    private record CacheEntry(EmbeddingResponse response, long timestamp) {}
}