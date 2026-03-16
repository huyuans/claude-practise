package com.huyuans.bailian.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;















@DisplayName("Token工具类测试")
class TokenUtilsTest {

    @Test
    @DisplayName("空文本返回0")
    void testEmptyText() {
        assertEquals(0, TokenUtils.estimateTokens(""));
        assertEquals(0, TokenUtils.estimateTokens(null));
    }

    @Test
    @DisplayName("纯中文文本估算")
    void testChineseText() {
        
        String text = "你好世界";
        int tokens = TokenUtils.estimateTokens(text);
        assertTrue(tokens >= 4, "中文文本应该有token");
        assertTrue(tokens <= 10, "4个中文字符估算值应该合理");
    }

    @Test
    @DisplayName("纯英文文本估算")
    void testEnglishText() {
        
        String text = "hello world test";
        int tokens = TokenUtils.estimateTokens(text);
        assertTrue(tokens >= 3, "3个英文单词应该有token");
        assertTrue(tokens <= 10, "3个英文单词估算值应该合理");
    }

    @Test
    @DisplayName("中英混合文本估算")
    void testMixedText() {
        String text = "你好hello世界world";
        int tokens = TokenUtils.estimateTokens(text);
        assertTrue(tokens > 0, "混合文本应该有token");
    }

    @Test
    @DisplayName("带标点符号的文本估算")
    void testTextWithPunctuation() {
        String text = "你好，世界！Hello, World!";
        int tokens = TokenUtils.estimateTokens(text);
        assertTrue(tokens > 0, "带标点的文本应该有token");
    }

    @Test
    @DisplayName("多条消息总token估算")
    void testEstimateMessagesTokens() {
        List<String> messages = Arrays.asList("你好", "hello");
        int tokens = TokenUtils.estimateMessagesTokens(messages);
        assertTrue(tokens > 0, "多条消息应该有token");
        
        assertTrue(tokens >= 7, "应该包含消息开销");
    }

    @Test
    @DisplayName("空消息列表返回0")
    void testEmptyMessages() {
        assertEquals(0, TokenUtils.estimateMessagesTokens(null));
        assertEquals(0, TokenUtils.estimateMessagesTokens(Collections.emptyList()));
    }

    @Test
    @DisplayName("输出token建议")
    void testSuggestOutputTokens() {
        int inputTokens = 100;
        int outputTokens = TokenUtils.suggestOutputTokens(inputTokens, 1.5);
        assertEquals(150, outputTokens, "按比例计算输出token");
    }

    @Test
    @DisplayName("默认maxTokens建议")
    void testSuggestMaxTokens() {
        int inputTokens = 200;
        int maxTokens = TokenUtils.suggestMaxTokens(inputTokens);
        assertEquals(200, maxTokens, "默认比例1.0");
    }

    @Test
    @DisplayName("检查是否超过限制")
    void testExceedsLimit() {
        String text = "这是一个测试文本";
        assertFalse(TokenUtils.exceedsLimit(text, 100), "短文本不应超过大限制");
        assertTrue(TokenUtils.exceedsLimit(text, 1), "长文本应超过小限制");
    }

    @Test
    @DisplayName("截断文本到限制内")
    void testTruncateToLimit() {
        String text = "这是一个比较长的测试文本，用于测试截断功能是否正常工作";
        String truncated = TokenUtils.truncateToLimit(text, 10);
        assertNotNull(truncated, "截断结果不应为null");
        assertTrue(truncated.length() <= text.length(), "截断后长度应小于等于原长度");
    }

    @Test
    @DisplayName("截断空文本返回原值")
    void testTruncateEmptyText() {
        assertEquals("", TokenUtils.truncateToLimit("", 100));
        assertNull(TokenUtils.truncateToLimit(null, 100));
    }

    @Test
    @DisplayName("不超限文本不截断")
    void testNoTruncateIfNeeded() {
        String text = "短文本";
        String result = TokenUtils.truncateToLimit(text, 100);
        assertEquals(text, result, "不超限应返回原文本");
    }

    @Test
    @DisplayName("格式化token数量")
    void testFormatTokens() {
        assertEquals("100 tokens", TokenUtils.formatTokens(100));
        assertEquals("1.0K tokens", TokenUtils.formatTokens(1000));
        assertEquals("1.5K tokens", TokenUtils.formatTokens(1500));
        assertEquals("1.00M tokens", TokenUtils.formatTokens(1000000));
    }

    @Test
    @DisplayName("数字文本估算")
    void testNumericText() {
        String text = "12345 67890";
        int tokens = TokenUtils.estimateTokens(text);
        assertTrue(tokens > 0, "数字文本应该有token");
    }

    @Test
    @DisplayName("特殊字符文本估算")
    void testSpecialCharacters() {
        String text = "!!! ??? @@@ ###";
        int tokens = TokenUtils.estimateTokens(text);
        assertTrue(tokens > 0, "特殊字符文本应该有token");
    }

    @Test
    @DisplayName("长文本估算")
    void testLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("测试");
        }
        String text = sb.toString();
        int tokens = TokenUtils.estimateTokens(text);
        assertTrue(tokens > 1000, "长文本应该有大量token");
    }
}