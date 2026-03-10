package com.huyuans.bailian.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BailianMetricsRecorder 单元测试
 */
@DisplayName("指标记录器测试")
class BailianMetricsRecorderTest {

    private MeterRegistry meterRegistry;
    private BailianMetricsRecorder recorder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        recorder = new BailianMetricsRecorder(meterRegistry);
    }

    @Test
    @DisplayName("记录聊天请求成功")
    void testRecordChatRequestSuccess() {
        recorder.recordChatRequest("qwen-turbo", true, 1000L, 100L);

        Counter requests = meterRegistry.find("bailian.chat.requests").counter();
        assertNotNull(requests);
        assertEquals(1.0, requests.count());

        Timer duration = meterRegistry.find("bailian.chat.duration").timer();
        assertNotNull(duration);
        assertEquals(1, duration.count());
    }

    @Test
    @DisplayName("记录聊天请求失败")
    void testRecordChatRequestFailure() {
        recorder.recordChatRequest("qwen-turbo", false, 500L, null);

        Counter errors = meterRegistry.find("bailian.chat.errors").counter();
        assertNotNull(errors);
        assertEquals(1.0, errors.count());
    }

    @Test
    @DisplayName("记录token数量")
    void testRecordTokens() {
        recorder.recordChatRequest("qwen-turbo", true, 100L, 150L);

        Counter tokens = meterRegistry.find("bailian.chat.tokens").counter();
        assertNotNull(tokens);
        assertEquals(150.0, tokens.count());
    }

    @Test
    @DisplayName("记录流式请求")
    void testRecordStreamRequest() {
        recorder.recordStreamRequest("qwen-turbo", true, 2000L);

        Counter requests = meterRegistry.find("bailian.stream.requests").counter();
        assertNotNull(requests);
        assertEquals(1.0, requests.count());

        Timer duration = meterRegistry.find("bailian.stream.duration").timer();
        assertNotNull(duration);
        assertEquals(1, duration.count());
    }

    @Test
    @DisplayName("记录流式请求失败")
    void testRecordStreamRequestFailure() {
        recorder.recordStreamRequest("qwen-turbo", false, 1000L);

        Counter errors = meterRegistry.find("bailian.stream.errors").counter();
        assertNotNull(errors);
        assertEquals(1.0, errors.count());
    }

    @Test
    @DisplayName("记录嵌入请求")
    void testRecordEmbeddingRequest() {
        recorder.recordEmbeddingRequest("text-embedding-v3", true, 500L, 5);

        Counter requests = meterRegistry.find("bailian.embedding.requests").counter();
        assertNotNull(requests);
        assertEquals(1.0, requests.count());

        Timer duration = meterRegistry.find("bailian.embedding.duration").timer();
        assertNotNull(duration);
        assertEquals(1, duration.count());
    }

    @Test
    @DisplayName("记录嵌入请求失败")
    void testRecordEmbeddingRequestFailure() {
        recorder.recordEmbeddingRequest("text-embedding-v3", false, 200L, 1);

        Counter errors = meterRegistry.find("bailian.embedding.errors").counter();
        assertNotNull(errors);
        assertEquals(1.0, errors.count());
    }

    @Test
    @DisplayName("记录缓存命中")
    void testRecordCacheHit() {
        recorder.recordEmbeddingCacheHit();

        Counter hits = meterRegistry.find("bailian.embedding.cache.hits").counter();
        assertNotNull(hits);
        assertEquals(1.0, hits.count());
    }

    @Test
    @DisplayName("记录缓存未命中")
    void testRecordCacheMiss() {
        recorder.recordEmbeddingCacheMiss();

        Counter misses = meterRegistry.find("bailian.embedding.cache.misses").counter();
        assertNotNull(misses);
        assertEquals(1.0, misses.count());
    }

    @Test
    @DisplayName("空MeterRegistry时安全处理")
    void testNullMeterRegistry() {
        BailianMetricsRecorder nullRecorder = new BailianMetricsRecorder(null);
        
        // 不应抛出异常
        assertDoesNotThrow(() -> {
            nullRecorder.recordChatRequest("model", true, 100L, 10L);
            nullRecorder.recordStreamRequest("model", true, 100L);
            nullRecorder.recordEmbeddingRequest("model", true, 100L, 1);
            nullRecorder.recordEmbeddingCacheHit();
            nullRecorder.recordEmbeddingCacheMiss();
        });
    }

    @Test
    @DisplayName("检查是否启用")
    void testIsEnabled() {
        assertTrue(recorder.isEnabled());
        
        BailianMetricsRecorder nullRecorder = new BailianMetricsRecorder(null);
        assertFalse(nullRecorder.isEnabled());
    }

    @Test
    @DisplayName("多次记录累加")
    void testMultipleRecords() {
        recorder.recordChatRequest("qwen-turbo", true, 100L, 50L);
        recorder.recordChatRequest("qwen-turbo", true, 100L, 50L);
        recorder.recordChatRequest("qwen-turbo", true, 100L, 50L);

        Counter requests = meterRegistry.find("bailian.chat.requests").counter();
        assertEquals(3.0, requests.count());

        Counter tokens = meterRegistry.find("bailian.chat.tokens").counter();
        assertEquals(150.0, tokens.count());
    }

    @Test
    @DisplayName("不同模型分别记录")
    void testDifferentModels() {
        recorder.recordChatRequest("qwen-turbo", true, 100L, 10L);
        recorder.recordChatRequest("qwen-max", true, 100L, 20L);

        // 验证有记录（不同模型会有不同的tag）
        Counter counter = meterRegistry.find("bailian.chat.requests").counter();
        assertNotNull(counter);
    }
}