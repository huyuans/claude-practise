package com.huyuans.bailian.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BailianMetricsRecorder.
 */
class BailianMetricsRecorderTest {

    private MeterRegistry meterRegistry;
    private BailianMetricsRecorder recorder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        recorder = new BailianMetricsRecorder(meterRegistry);
    }

    @Test
    void testRecordChatRequestSuccess() {
        recorder.recordChatRequest("qwen-turbo", true, 1000L, 100L);

        assertEquals(1.0, meterRegistry.find("bailian.chat.requests").counter().count());
        assertEquals(1, meterRegistry.find("bailian.chat.duration").timer().count());
    }

    @Test
    void testRecordChatRequestFailure() {
        recorder.recordChatRequest("qwen-turbo", false, 500L, null);

        assertEquals(1.0, meterRegistry.find("bailian.chat.errors").counter().count());
    }

    @Test
    void testRecordStreamRequest() {
        recorder.recordStreamRequest("qwen-turbo", true, 2000L);

        assertEquals(1.0, meterRegistry.find("bailian.stream.requests").counter().count());
    }

    @Test
    void testRecordEmbeddingRequest() {
        recorder.recordEmbeddingRequest("text-embedding-v3", true, 500L, 5);

        assertEquals(1.0, meterRegistry.find("bailian.embedding.requests").counter().count());
    }

    @Test
    void testRecordCache() {
        recorder.recordEmbeddingCacheHit();
        recorder.recordEmbeddingCacheMiss();

        assertEquals(1.0, meterRegistry.find("bailian.embedding.cache.hits").counter().count());
        assertEquals(1.0, meterRegistry.find("bailian.embedding.cache.misses").counter().count());
    }

    @Test
    void testNullMeterRegistry() {
        BailianMetricsRecorder nullRecorder = new BailianMetricsRecorder(null);

        assertDoesNotThrow(() -> {
            nullRecorder.recordChatRequest("model", true, 100L, 10L);
            nullRecorder.recordStreamRequest("model", true, 100L);
            nullRecorder.recordEmbeddingCacheHit();
        });
        assertFalse(nullRecorder.isEnabled());
    }

    @Test
    void testMultipleRecords() {
        recorder.recordChatRequest("qwen-turbo", true, 100L, 50L);
        recorder.recordChatRequest("qwen-turbo", true, 100L, 50L);
        recorder.recordChatRequest("qwen-turbo", true, 100L, 50L);

        assertEquals(3.0, meterRegistry.find("bailian.chat.requests").counter().count());
        assertEquals(150.0, meterRegistry.find("bailian.chat.tokens").counter().count());
    }
}
