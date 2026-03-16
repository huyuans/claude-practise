package com.huyuans.bailian.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 百炼 API 指标记录器
 * <p>
 * 基于 Micrometer 实现指标收集，支持 Prometheus、Graphana 等监控系统。
 * 自动记录 API 调用次数、延迟、token 使用量、缓存命中率等关键指标。
 * <p>
 * 指标命名规范：
 * <ul>
 *   <li>bailian.chat.requests - 聊天请求计数</li>
 *   <li>bailian.chat.errors - 聊天错误计数</li>
 *   <li>bailian.chat.duration - 聊天延迟分布</li>
 *   <li>bailian.chat.tokens - Token 使用量</li>
 *   <li>bailian.embedding.cache.hits - 缓存命中次数</li>
 * </ul>
 * <p>
 * 使用方式：注入依赖 Spring Boot Actuator 和 Micrometer
 *
 * @author Kasper
 * @since 1.0.0
 * @see MeterRegistry Micrometer 核心接口
 */
@Slf4j
public class BailianMetricsRecorder {

    /** 指标名称前缀 */
    private static final String PREFIX = "bailian.";

    /** Micrometer 注册表 */
    private final MeterRegistry meterRegistry;

    // ==================== 指标名称常量 ====================
    
    /** 聊天请求计数 */
    private static final String CHAT_REQUESTS = PREFIX + "chat.requests";
    /** 聊天错误计数 */
    private static final String CHAT_ERRORS = PREFIX + "chat.errors";
    /** 聊天延迟 */
    private static final String CHAT_DURATION = PREFIX + "chat.duration";
    /** Token 使用量 */
    private static final String CHAT_TOKENS = PREFIX + "chat.tokens";

    /** 流式请求计数 */
    private static final String STREAM_REQUESTS = PREFIX + "stream.requests";
    /** 流式错误计数 */
    private static final String STREAM_ERRORS = PREFIX + "stream.errors";
    /** 流式延迟 */
    private static final String STREAM_DURATION = PREFIX + "stream.duration";

    /** Embedding 请求计数 */
    private static final String EMBEDDING_REQUESTS = PREFIX + "embedding.requests";
    /** Embedding 错误计数 */
    private static final String EMBEDDING_ERRORS = PREFIX + "embedding.errors";
    /** Embedding 延迟 */
    private static final String EMBEDDING_DURATION = PREFIX + "embedding.duration";
    /** Embedding 缓存命中 */
    private static final String EMBEDDING_CACHE_HITS = PREFIX + "embedding.cache.hits";
    /** Embedding 缓存未命中 */
    private static final String EMBEDDING_CACHE_MISSES = PREFIX + "embedding.cache.misses";

    public BailianMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        log.info("百炼指标记录器已初始化");
    }

    /**
     * 记录聊天请求
     *
     * @param model     模型名称
     * @param success   是否成功
     * @param duration  耗时（毫秒）
     * @param tokens    token 数量（可选）
     */
    public void recordChatRequest(String model, boolean success, long duration, Long tokens) {
        if (meterRegistry == null) return;

        Counter.builder(CHAT_REQUESTS)
                .tag("model", model)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();

        Timer.builder(CHAT_DURATION)
                .tag("model", model)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);

        if (!success) {
            Counter.builder(CHAT_ERRORS)
                    .tag("model", model)
                    .register(meterRegistry)
                    .increment();
        }

        if (tokens != null && tokens > 0) {
            Counter.builder(CHAT_TOKENS)
                    .tag("model", model)
                    .register(meterRegistry)
                    .increment(tokens);
        }

        log.debug("记录聊天指标: model={}, success={}, duration={}ms, tokens={}", 
                model, success, duration, tokens);
    }

    /**
     * 记录流式聊天请求
     *
     * @param model    模型名称
     * @param success  是否成功
     * @param duration 耗时（毫秒）
     */
    public void recordStreamRequest(String model, boolean success, long duration) {
        if (meterRegistry == null) return;

        Counter.builder(STREAM_REQUESTS)
                .tag("model", model)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();

        Timer.builder(STREAM_DURATION)
                .tag("model", model)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);

        if (!success) {
            Counter.builder(STREAM_ERRORS)
                    .tag("model", model)
                    .register(meterRegistry)
                    .increment();
        }

        log.debug("记录流式聊天指标: model={}, success={}, duration={}ms", model, success, duration);
    }

    /**
     * 记录嵌入请求
     *
     * @param model    模型名称
     * @param success  是否成功
     * @param duration 耗时（毫秒）
     * @param textCount 文本数量
     */
    public void recordEmbeddingRequest(String model, boolean success, long duration, int textCount) {
        if (meterRegistry == null) return;

        Counter.builder(EMBEDDING_REQUESTS)
                .tag("model", model)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
                .increment();

        Timer.builder(EMBEDDING_DURATION)
                .tag("model", model)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);

        if (!success) {
            Counter.builder(EMBEDDING_ERRORS)
                    .tag("model", model)
                    .register(meterRegistry)
                    .increment();
        }

        log.debug("记录嵌入指标: model={}, success={}, duration={}ms, textCount={}", 
                model, success, duration, textCount);
    }

    /**
     * 记录嵌入缓存命中
     */
    public void recordEmbeddingCacheHit() {
        if (meterRegistry == null) return;
        Counter.builder(EMBEDDING_CACHE_HITS).register(meterRegistry).increment();
    }

    /**
     * 记录嵌入缓存未命中
     */
    public void recordEmbeddingCacheMiss() {
        if (meterRegistry == null) return;
        Counter.builder(EMBEDDING_CACHE_MISSES).register(meterRegistry).increment();
    }

    /**
     * 是否已启用指标记录
     */
    public boolean isEnabled() {
        return meterRegistry != null;
    }
}