package com.huyuans.bailian.session;

import com.huyuans.bailian.model.request.ChatRequest;
import com.huyuans.bailian.model.response.ChatResponse;
import com.huyuans.bailian.service.BailianService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话会话管理器
 * <p>
 * 支持多会话管理、自动过期清理、上下文长度限制。
 * 适用于需要维护多轮对话状态的应用场景，如聊天机器人、客服系统等。
 * <p>
 * 使用示例：
 * <pre>
 * // 创建会话
 * String sessionId = conversationManager.createSession("你是一个助手");
 * 
 * // 多轮对话
 * String reply1 = conversationManager.chat(sessionId, "你好").block();
 * String reply2 = conversationManager.chat(sessionId, "我刚才说了什么").block();
 * 
 * // 清空会话历史（保留会话）
 * conversationManager.clearSession(sessionId);
 * 
 * // 删除会话
 * conversationManager.removeSession(sessionId);
 * </pre>
 * <p>
 * 配置示例：
 * <pre>
 * bailian:
 *   conversation:
 *     enabled: true
 *     expire-minutes: 30    # 会话过期时间
 *     max-tokens-per-session: 4000  # 单会话最大 token 数
 * </pre>
 *
 * @author Kasper
 * @since 1.0.0
 */
@Slf4j
public class ConversationManager {

    /** 百炼服务 */
    private final BailianService bailianService;
    
    /** 会话存储（sessionId -> Session） */
    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();
    
    /**
     * 会话过期时间（分钟）
     */
    private long expireMinutes = 60;
    
    /**
     * 单会话最大 token 数
     */
    private int maxTokensPerSession = 4000;

    public ConversationManager(BailianService bailianService) {
        this.bailianService = bailianService;
        startCleanupTask();
    }

    public ConversationManager(BailianService bailianService, long expireMinutes, int maxTokensPerSession) {
        this.bailianService = bailianService;
        this.expireMinutes = expireMinutes;
        this.maxTokensPerSession = maxTokensPerSession;
        startCleanupTask();
    }

    /**
     * 创建新会话
     *
     * @return 会话ID
     */
    public String createSession() {
        return createSession(null);
    }

    /**
     * 创建新会话（带系统提示词）
     *
     * @param systemPrompt 系统提示词
     * @return 会话ID
     */
    public String createSession(String systemPrompt) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        ConversationSession session = new ConversationSession(sessionId, systemPrompt);
        session.setMaxTokens(maxTokensPerSession);
        sessions.put(sessionId, session);
        log.debug("创建会话: {}", sessionId);
        return sessionId;
    }

    /**
     * 对话（自动管理历史）
     *
     * @param sessionId   会话ID
     * @param userMessage 用户消息
     * @return 助手回复内容
     */
    public Mono<String> chat(String sessionId, String userMessage) {
        ConversationSession session = sessions.get(sessionId);
        if (session == null) {
            return Mono.error(new IllegalArgumentException("会话不存在: " + sessionId));
        }

        session.addUserMessage(userMessage);

        return bailianService.chat(ChatRequest.builder()
                .messages(session.getAllMessages())
                .build())
                .map(response -> {
                    String content = extractContent(response);
                    if (content != null && !content.isEmpty()) {
                        session.addAssistantMessage(content);
                    }
                    return content;
                });
    }

    /**
     * 对话（返回完整响应）
     *
     * @param sessionId   会话ID
     * @param userMessage 用户消息
     * @return 完整聊天响应
     */
    public Mono<ChatResponse> chatFull(String sessionId, String userMessage) {
        ConversationSession session = sessions.get(sessionId);
        if (session == null) {
            return Mono.error(new IllegalArgumentException("会话不存在: " + sessionId));
        }

        session.addUserMessage(userMessage);

        return bailianService.chat(ChatRequest.builder()
                .messages(session.getAllMessages())
                .build())
                .doOnSuccess(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        String content = response.getChoices().get(0).getMessage().getContent();
                        session.addAssistantMessage(content);
                    }
                });
    }

    /**
     * 获取会话
     */
    public Optional<ConversationSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 获取会话历史
     */
    public Optional<List<ChatRequest.Message>> getHistory(String sessionId) {
        return getSession(sessionId).map(ConversationSession::getMessages);
    }

    /**
     * 清空会话历史（保留会话和系统提示词）
     */
    public void clearSession(String sessionId) {
        getSession(sessionId).ifPresent(ConversationSession::clear);
        log.debug("清空会话历史: {}", sessionId);
    }

    /**
     * 删除会话
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log.debug("删除会话: {}", sessionId);
    }

    /**
     * 设置系统提示词
     */
    public void setSystemPrompt(String sessionId, String systemPrompt) {
        getSession(sessionId).ifPresent(s -> s.setSystemPrompt(systemPrompt));
    }

    /**
     * 获取活跃会话数
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        int removed = 0;
        for (Map.Entry<String, ConversationSession> entry : sessions.entrySet()) {
            if (entry.getValue().isExpired(expireMinutes)) {
                sessions.remove(entry.getKey());
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("清理过期会话: {} 个", removed);
        }
    }

    /**
     * 启动定时清理任务
     */
    private void startCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(expireMinutes * 60 * 1000 / 2); // 每半个过期周期清理一次
                    cleanupExpiredSessions();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "conversation-cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    private String extractContent(ChatResponse response) {
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return "";
    }
}