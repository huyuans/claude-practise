package com.huyuans.bailian.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ContextCompressor {

    private static final Logger log = Logger.getLogger(ContextCompressor.class.getName());

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这",
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "must", "shall", "can", "need", "dare", "ought", "used", "to", "of", "in", "for", "on", "with", "at", "by", "from", "as", "into", "through", "during", "before", "after", "above", "below", "between", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "just", "and", "but", "if", "or", "because", "until", "while"
    ));

    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[。！？.!?]+");

    private final int maxTokens;
    private final CompressionStrategy strategy;
    private final double preserveRatio;
    private final boolean preserveSystemPrompt;
    private final boolean preserveRecentMessages;
    private final int recentMessageCount;

    public ContextCompressor(int maxTokens, CompressionStrategy strategy, double preserveRatio,
                            boolean preserveSystemPrompt, boolean preserveRecentMessages, int recentMessageCount) {
        this.maxTokens = maxTokens;
        this.strategy = strategy;
        this.preserveRatio = preserveRatio;
        this.preserveSystemPrompt = preserveSystemPrompt;
        this.preserveRecentMessages = preserveRecentMessages;
        this.recentMessageCount = recentMessageCount;
        log.info("上下文压缩器初始化: maxTokens=" + maxTokens + ", strategy=" + strategy);
    }

    public static ContextCompressor create(int maxTokens) {
        return new ContextCompressor(maxTokens, CompressionStrategy.SUMMARIZE, 0.3, true, true, 2);
    }

    public CompressionResult compress(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return new CompressionResult(new ArrayList<>(), 0, 0, 0, 0, 0.0);
        }

        int estimatedTokens = estimateTokens(messages);

        if (estimatedTokens <= maxTokens) {
            return new CompressionResult(new ArrayList<>(messages), messages.size(), messages.size(),
                    estimatedTokens, estimatedTokens, 1.0);
        }

        List<Message> preserved = new ArrayList<>();
        List<Message> toCompress = new ArrayList<>();

        partitionMessages(messages, preserved, toCompress);

        List<Message> compressed = switch (strategy) {
            case TRUNCATE -> truncateMessages(toCompress, preserved);
            case SUMMARIZE -> summarizeMessages(toCompress, preserved);
            case SLIDING_WINDOW -> slidingWindowCompress(toCompress, preserved);
        };

        int newTokenCount = estimateTokens(compressed);
        double ratio = (double) newTokenCount / estimatedTokens;

        log.info("压缩完成: " + messages.size() + " 条消息 -> " + compressed.size() + " 条");

        return new CompressionResult(compressed, messages.size(), compressed.size(),
                estimatedTokens, newTokenCount, ratio);
    }

    private void partitionMessages(List<Message> messages, List<Message> preserved, List<Message> toCompress) {
        int total = messages.size();
        int recentStart = Math.max(0, total - recentMessageCount);

        for (int i = 0; i < total; i++) {
            Message msg = messages.get(i);
            if (preserveSystemPrompt && "system".equals(msg.getRole())) {
                preserved.add(msg);
            } else if (preserveRecentMessages && i >= recentStart) {
                preserved.add(msg);
            } else {
                toCompress.add(msg);
            }
        }
    }

    private List<Message> truncateMessages(List<Message> toCompress, List<Message> preserved) {
        List<Message> result = new ArrayList<>(preserved);
        int remainingTokens = maxTokens - estimateTokens(preserved);

        for (int i = toCompress.size() - 1; i >= 0 && remainingTokens > 0; i--) {
            Message msg = toCompress.get(i);
            int msgTokens = estimateTokens(msg);
            if (msgTokens <= remainingTokens) {
                result.add(0, msg);
                remainingTokens -= msgTokens;
            }
        }

        return result;
    }

    private List<Message> summarizeMessages(List<Message> toCompress, List<Message> preserved) {
        List<Message> result = new ArrayList<>(preserved);

        if (!toCompress.isEmpty()) {
            String summary = generateSummary(toCompress);
            Message summaryMessage = new Message("system", "[历史对话摘要] " + summary);

            if (!result.isEmpty() && "system".equals(result.get(0).getRole())) {
                result.add(1, summaryMessage);
            } else {
                result.add(0, summaryMessage);
            }
        }

        return result;
    }

    private List<Message> slidingWindowCompress(List<Message> toCompress, List<Message> preserved) {
        List<Message> result = new ArrayList<>();
        int windowSize = (int) (toCompress.size() * preserveRatio);
        int step = toCompress.size() / (windowSize > 0 ? windowSize : 1);

        for (int i = 0; i < toCompress.size(); i += step) {
            if (result.size() >= windowSize) break;
            result.add(toCompress.get(i));
        }

        result.addAll(preserved);
        return result;
    }

    private String generateSummary(List<Message> messages) {
        StringBuilder summary = new StringBuilder();
        Map<String, List<String>> roleMessages = new HashMap<>();

        for (Message msg : messages) {
            roleMessages.computeIfAbsent(msg.getRole(), k -> new ArrayList<>())
                    .add(msg.getContent());
        }

        for (Map.Entry<String, List<String>> entry : roleMessages.entrySet()) {
            summary.append(entry.getKey()).append(": ");
            String combined = String.join("; ", entry.getValue());
            summary.append(extractKeyPhrases(combined));
            summary.append(". ");
        }

        return summary.toString().trim();
    }

    private String extractKeyPhrases(String text) {
        String[] sentences = SENTENCE_PATTERN.split(text);
        if (sentences.length <= 2) {
            return text.length() > 100 ? text.substring(0, 100) + "..." : text;
        }

        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : text.split("\\s+")) {
            String lower = word.toLowerCase().replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", "");
            if (!STOP_WORDS.contains(lower) && lower.length() > 1) {
                wordFreq.merge(lower, 1, Integer::sum);
            }
        }

        StringBuilder keyPhrases = new StringBuilder();
        for (String sentence : sentences) {
            if (containsKeyWords(sentence, wordFreq, 2)) {
                if (keyPhrases.length() > 0) {
                    keyPhrases.append("; ");
                }
                keyPhrases.append(sentence.trim());
            }
        }

        String result = keyPhrases.toString();
        return result.length() > 200 ? result.substring(0, 200) + "..." : result;
    }

    private boolean containsKeyWords(String sentence, Map<String, Integer> wordFreq, int threshold) {
        int count = 0;
        for (String word : sentence.split("\\s+")) {
            String lower = word.toLowerCase().replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", "");
            if (wordFreq.getOrDefault(lower, 0) >= 2) {
                count++;
            }
        }
        return count >= threshold;
    }

    private int estimateTokens(List<Message> messages) {
        return messages.stream().mapToInt(this::estimateTokens).sum();
    }

    private int estimateTokens(Message message) {
        if (message == null || message.getContent() == null) {
            return 0;
        }
        String content = message.getContent();
        int chineseChars = 0;

        for (char c : content.toCharArray()) {
            if (Character.toString(c).matches("[\u4e00-\u9fa5]")) {
                chineseChars++;
            }
        }

        String[] words = content.split("\\s+");
        int englishWords = words.length;

        return (int) (chineseChars * 1.5 + englishWords * 1.3);
    }

    public enum CompressionStrategy {
        TRUNCATE,
        SUMMARIZE,
        SLIDING_WINDOW
    }

    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }

        public static Message of(String role, String content) {
            return new Message(role, content);
        }
    }

    public static class CompressionResult {
        private final List<Message> messages;
        private final int originalCount;
        private final int compressedCount;
        private final int originalTokens;
        private final int compressedTokens;
        private final double compressionRatio;

        public CompressionResult(List<Message> messages, int originalCount, int compressedCount,
                                int originalTokens, int compressedTokens, double compressionRatio) {
            this.messages = messages;
            this.originalCount = originalCount;
            this.compressedCount = compressedCount;
            this.originalTokens = originalTokens;
            this.compressedTokens = compressedTokens;
            this.compressionRatio = compressionRatio;
        }

        public List<Message> getMessages() { return messages; }
        public int getOriginalCount() { return originalCount; }
        public int getCompressedCount() { return compressedCount; }
        public int getOriginalTokens() { return originalTokens; }
        public int getCompressedTokens() { return compressedTokens; }
        public double getCompressionRatio() { return compressionRatio; }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int maxTokens = 4000;
        private CompressionStrategy strategy = CompressionStrategy.SUMMARIZE;
        private double preserveRatio = 0.3;
        private boolean preserveSystemPrompt = true;
        private boolean preserveRecentMessages = true;
        private int recentMessageCount = 2;

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder strategy(CompressionStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder preserveRatio(double preserveRatio) {
            this.preserveRatio = preserveRatio;
            return this;
        }

        public ContextCompressor build() {
            return new ContextCompressor(maxTokens, strategy, preserveRatio,
                    preserveSystemPrompt, preserveRecentMessages, recentMessageCount);
        }
    }
}