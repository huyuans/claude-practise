package com.huyuans.bailian.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("上下文压缩器测试")
class ContextCompressorTest {

    private ContextCompressor compressor;

    @BeforeEach
    void setUp() {
        compressor = ContextCompressor.create(100);
    }

    @Test
    @DisplayName("空消息列表测试")
    void testEmptyMessages() {
        ContextCompressor.CompressionResult result = compressor.compress(Collections.emptyList());
        assertTrue(result.getMessages().isEmpty());
    }

    @Test
    @DisplayName("未超限消息不压缩测试")
    void testNoCompressionNeeded() {
        ContextCompressor.CompressionResult result = compressor.compress(Arrays.asList(
                ContextCompressor.Message.of("user", "你好"),
                ContextCompressor.Message.of("assistant", "你好！")
        ));

        assertEquals(2, result.getMessages().size());
        assertEquals(1.0, result.getCompressionRatio());
    }

    @Test
    @DisplayName("Message静态工厂方法测试")
    void testMessageStaticFactory() {
        ContextCompressor.Message message = ContextCompressor.Message.of("user", "测试内容");
        assertEquals("user", message.getRole());
        assertEquals("测试内容", message.getContent());
    }

    @Test
    @DisplayName("Builder测试")
    void testBuilder() {
        ContextCompressor custom = ContextCompressor.builder()
                .maxTokens(2000)
                .strategy(ContextCompressor.CompressionStrategy.SUMMARIZE)
                .build();

        assertNotNull(custom);
    }
}