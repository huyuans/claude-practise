package com.huyuans.bailian.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContextCompressor.
 */
class ContextCompressorTest {

    private ContextCompressor compressor;

    @BeforeEach
    void setUp() {
        compressor = ContextCompressor.create(100);
    }

    @Test
    void testEmptyMessages() {
        ContextCompressor.CompressionResult result = compressor.compress(Collections.emptyList());
        assertTrue(result.getMessages().isEmpty());
    }

    @Test
    void testNoCompressionNeeded() {
        ContextCompressor.CompressionResult result = compressor.compress(Arrays.asList(
                ContextCompressor.Message.of("user", "Hello"),
                ContextCompressor.Message.of("assistant", "Hi there!")
        ));

        assertEquals(2, result.getMessages().size());
        assertEquals(1.0, result.getCompressionRatio());
    }

    @Test
    void testMessageStaticFactory() {
        ContextCompressor.Message message = ContextCompressor.Message.of("user", "Test content");
        assertEquals("user", message.getRole());
        assertEquals("Test content", message.getContent());
    }

    @Test
    void testBuilder() {
        ContextCompressor custom = ContextCompressor.builder()
                .maxTokens(2000)
                .strategy(ContextCompressor.CompressionStrategy.SUMMARIZE)
                .build();

        assertNotNull(custom);
    }
}
