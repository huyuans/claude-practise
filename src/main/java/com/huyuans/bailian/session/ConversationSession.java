package com.huyuans.bailian.session;

import com.huyuans.bailian.model.request.ChatRequest;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 对话会话
 * <p>
 * 封装单次对话的上下文，包括消息历史和元数据
 *
 * @author Kasper
 * @since 1.0.0
 */
@Data
public class ConversationSession {

    /**
     * 会话ID
     */
    private final String sessionId;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 消息历史
     */
    private final List<ChatRequest.Message> messages = new ArrayList<>();

    /**
     * 创建时间
     */
    private final Instant createdAt = Instant.now();

    /**
     * 最后活跃时间
     */
    private Instant lastActiveAt = Instant.now();

    /**
     * 预估 token 数
     */
    private int estimatedTokens = 0;

    /**
     * 最大 token 数限制
     */
    private int maxTokens = 4000;

    public ConversationSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public ConversationSession(String sessionId, String systemPrompt) {
        this.sessionId = sessionId;
        this.systemPrompt = systemPrompt;
    }

    /**
     * 添加用户消息
     */
    public void addUserMessage(String content) {
        messages.add(ChatRequest.Message.user(content));
        lastActiveAt = Instant.now();
        estimatedTokens += estimateTokens(content);
        trimIfNeeded();
    }

    /**
     * 添加助手消息
     */
    public void addAssistantMessage(String content) {
        messages.add(ChatRequest.Message.assistant(content));
        lastActiveAt = Instant.now();
        estimatedTokens += estimateTokens(content);
    }

    /**
     * 获取所有消息（包含系统提示词）
     */
    public List<ChatRequest.Message> getAllMessages() {
        List<ChatRequest.Message> all = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            all.add(ChatRequest.Message.system(systemPrompt));
        }
        all.addAll(messages);
        return all;
    }

    /**
     * 清空历史
     */
    public void clear() {
        messages.clear();
        estimatedTokens = 0;
        lastActiveAt = Instant.now();
    }

    /**
     * 是否过期
     */
    public boolean isExpired(long expireMinutes) {
        return Instant.now().minusSeconds(expireMinutes * 60).isAfter(lastActiveAt);
    }

    /**
     * 超出 token 限制时截断旧消息
     */
    private void trimIfNeeded() {
        while (estimatedTokens > maxTokens && messages.size() > 2) {
            // 成对删除最早的对话（用户+助手）
            ChatRequest.Message removed = messages.remove(0);
            estimatedTokens -= estimateTokens(removed.getContent());
        }
    }

    /**
     * 粗略估算 token 数（中文约 1.5 字/token，英文约 4 字符/token）
     */
    private int estimateTokens(String text) {
        if (text == null) return 0;
        // 简单估算：每 3 个字符约 1 token
        return text.length() / 3 + 1;
    }
}