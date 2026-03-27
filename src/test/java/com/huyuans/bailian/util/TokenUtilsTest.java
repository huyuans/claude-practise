package com.huyuans.bailian.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TokenUtils.
 */
class TokenUtilsTest {

    @Test
    void testEmptyText() {
        assertEquals(0, TokenUtils.estimateTokens(""));
        assertEquals(0, TokenUtils.estimateTokens(null));
    }

    @Test
    void testChineseText() {
        int tokens = TokenUtils.estimateTokens("你好世界");
        assertTrue(tokens >= 4 && tokens <= 10);
    }

    @Test
    void testEnglishText() {
        int tokens = TokenUtils.estimateTokens("hello world test");
        assertTrue(tokens >= 3 && tokens <= 10);
    }

    @Test
    void testEstimateMessagesTokens() {
        int tokens = TokenUtils.estimateMessagesTokens(Arrays.asList("你好", "hello"));
        assertTrue(tokens > 0);
    }

    @Test
    void testEmptyMessages() {
        assertEquals(0, TokenUtils.estimateMessagesTokens(null));
        assertEquals(0, TokenUtils.estimateMessagesTokens(Collections.emptyList()));
    }

    @Test
    void testSuggestOutputTokens() {
        assertEquals(150, TokenUtils.suggestOutputTokens(100, 1.5));
        assertEquals(200, TokenUtils.suggestMaxTokens(200));
    }

    @Test
    void testExceedsLimit() {
        assertFalse(TokenUtils.exceedsLimit("short", 100));
        assertTrue(TokenUtils.exceedsLimit("long text here", 1));
    }

    @Test
    void testTruncateToLimit() {
        String text = "This is a long test text for truncation testing";
        String truncated = TokenUtils.truncateToLimit(text, 5);
        assertNotNull(truncated);
        assertTrue(truncated.length() <= text.length());
    }

    @Test
    void testFormatTokens() {
        assertEquals("100 tokens", TokenUtils.formatTokens(100));
        assertEquals("1.0K tokens", TokenUtils.formatTokens(1000));
        assertEquals("1.00M tokens", TokenUtils.formatTokens(1000000));
    }
}
