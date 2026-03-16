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



































@Slf4j
public class ConversationManager {

    
    private final BailianService bailianService;
    
    
    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();
    
    


    private long expireMinutes = 60;
    
    


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

    




    public String createSession() {
        return createSession(null);
    }

    





    public String createSession(String systemPrompt) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        ConversationSession session = new ConversationSession(sessionId, systemPrompt);
        session.setMaxTokens(maxTokensPerSession);
        sessions.put(sessionId, session);
        log.debug("创建会话: {}", sessionId);
        return sessionId;
    }

    






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

    


    public Optional<ConversationSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    


    public Optional<List<ChatRequest.Message>> getHistory(String sessionId) {
        return getSession(sessionId).map(ConversationSession::getMessages);
    }

    


    public void clearSession(String sessionId) {
        getSession(sessionId).ifPresent(ConversationSession::clear);
        log.debug("清空会话历史: {}", sessionId);
    }

    


    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log.debug("删除会话: {}", sessionId);
    }

    


    public void setSystemPrompt(String sessionId, String systemPrompt) {
        getSession(sessionId).ifPresent(s -> s.setSystemPrompt(systemPrompt));
    }

    


    public int getActiveSessionCount() {
        return sessions.size();
    }

    


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

    


    private void startCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(expireMinutes * 60 * 1000 / 2); 
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