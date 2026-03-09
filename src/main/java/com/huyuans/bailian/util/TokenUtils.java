package com.huyuans.bailian.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Token 计数工具
 * <p>
 * 提供基于规则的 token 估算功能，用于快速估算文本的 token 数量。
 * 注意：这是估算值，实际值可能有 5-10% 的误差。
 * 精确计数需要使用分词器（如 tiktoken）。
 *
 * @author Kasper
 * @since 1.0.0
 */
public class TokenUtils {

    private TokenUtils() {}

    /**
     * 中文字符的正则（CJK 统一表意文字）
     */
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fff\\u3400-\\u4dbf]");

    /**
     * 估算文本的 token 数量
     * <p>
     * 估算规则：
     * - 中文字符：约 1.5 tokens/字
     * - 英文单词：约 1.3 tokens/词
     * - 标点符号和空格：约 0.5 tokens/个
     *
     * @param text 待估算的文本
     * @return 估算的 token 数量
     */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int chineseCount = 0;
        int wordCount = 0;
        int otherCount = 0;

        StringBuilder currentWord = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (CHINESE_PATTERN.matcher(String.valueOf(c)).matches()) {
                // 中文字符
                if (currentWord.length() > 0) {
                    wordCount++;
                    currentWord.setLength(0);
                }
                chineseCount++;
            } else if (Character.isLetterOrDigit(c)) {
                // 英文/数字字符，累积成单词
                currentWord.append(c);
            } else {
                // 其他字符（标点、空格等）
                if (currentWord.length() > 0) {
                    wordCount++;
                    currentWord.setLength(0);
                }
                otherCount++;
            }
        }

        // 处理最后一个单词
        if (currentWord.length() > 0) {
            wordCount++;
        }

        // 计算估算值
        // 中文：约 1.5 tokens/字
        // 英文单词：约 1.3 tokens/词
        // 其他：约 0.5 tokens/个
        return (int) Math.ceil(chineseCount * 1.5 + wordCount * 1.3 + otherCount * 0.5);
    }

    /**
     * 估算多条消息的总 token 数量
     *
     * @param messages 消息列表
     * @return 估算的总 token 数量
     */
    public static int estimateMessagesTokens(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (String message : messages) {
            total += estimateTokens(message);
            // 每条消息额外约 4 tokens 的开销（角色标记等）
            total += 4;
        }

        // 请求整体额外约 3 tokens 的开销
        return total + 3;
    }

    /**
     * 根据输入 token 数估算输出 token 数
     * <p>
     * 一般输出 token 数建议为输入 token 数的 0.5-2 倍
     *
     * @param inputTokens 输入 token 数
     * @param ratio       输出/输入比例
     * @return 建议的输出 token 数
     */
    public static int suggestOutputTokens(int inputTokens, double ratio) {
        return (int) Math.ceil(inputTokens * ratio);
    }

    /**
     * 根据输入 token 数估算建议的 maxTokens 参数
     * <p>
     * 默认比例为 1.0，即输出长度约等于输入长度
     *
     * @param inputTokens 输入 token 数
     * @return 建议的 maxTokens 值
     */
    public static int suggestMaxTokens(int inputTokens) {
        return suggestOutputTokens(inputTokens, 1.0);
    }

    /**
     * 检查文本是否超过指定的 token 限制
     *
     * @param text    待检查的文本
     * @param limit   token 限制
     * @return 是否超过限制
     */
    public static boolean exceedsLimit(String text, int limit) {
        return estimateTokens(text) > limit;
    }

    /**
     * 截断文本以适应 token 限制
     *
     * @param text  原始文本
     * @param limit token 限制
     * @return 截断后的文本
     */
    public static String truncateToLimit(String text, int limit) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (!exceedsLimit(text, limit)) {
            return text;
        }

        // 估算每个字符的平均 token 数
        double avgTokensPerChar = (double) estimateTokens(text) / text.length();
        // 目标字符数（留 10% 的安全余量）
        int targetChars = (int) ((limit * 0.9) / avgTokensPerChar);

        if (targetChars >= text.length()) {
            return text;
        }

        // 尝试在句子边界截断
        int lastPeriod = text.lastIndexOf('。');
        int lastQuestion = text.lastIndexOf('？');
        int lastExclaim = text.lastIndexOf('！');
        int lastDot = text.lastIndexOf('.');

        int lastSentenceEnd = Math.max(Math.max(lastPeriod, lastQuestion), 
                                        Math.max(lastExclaim, lastDot));

        if (lastSentenceEnd > targetChars * 0.7 && lastSentenceEnd < targetChars * 1.3) {
            return text.substring(0, lastSentenceEnd + 1);
        }

        return text.substring(0, targetChars);
    }

    /**
     * 格式化 token 数量为可读字符串
     *
     * @param tokens token 数量
     * @return 可读的字符串
     */
    public static String formatTokens(int tokens) {
        if (tokens < 1000) {
            return tokens + " tokens";
        } else if (tokens < 1000000) {
            return String.format("%.1fK tokens", tokens / 1000.0);
        } else {
            return String.format("%.2fM tokens", tokens / 1000000.0);
        }
    }
}