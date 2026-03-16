package com.huyuans.bailian.session;

import com.huyuans.bailian.model.request.ChatRequest;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

















@Data
public class ConversationSession {

    


    private final String sessionId;

    




    private String systemPrompt;

    




    private final List<ChatRequest.Message> messages = new ArrayList<>();

    


    private final Instant createdAt = Instant.now();

    




    private Instant lastActiveAt = Instant.now();

    




    private int estimatedTokens = 0;

    




    private int maxTokens = 4000;

    public ConversationSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public ConversationSession(String sessionId, String systemPrompt) {
        this.sessionId = sessionId;
        this.systemPrompt = systemPrompt;
    }

    


    public void addUserMessage(String content) {
        messages.add(ChatRequest.Message.user(content));
        lastActiveAt = Instant.now();
        estimatedTokens += estimateTokens(content);
        trimIfNeeded();
    }

    


    public void addAssistantMessage(String content) {
        messages.add(ChatRequest.Message.assistant(content));
        lastActiveAt = Instant.now();
        estimatedTokens += estimateTokens(content);
    }

    


    public List<ChatRequest.Message> getAllMessages() {
        List<ChatRequest.Message> all = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            all.add(ChatRequest.Message.system(systemPrompt));
        }
        all.addAll(messages);
        return all;
    }

    


    public void clear() {
        messages.clear();
        estimatedTokens = 0;
        lastActiveAt = Instant.now();
    }

    


    public boolean isExpired(long expireMinutes) {
        return Instant.now().minusSeconds(expireMinutes * 60).isAfter(lastActiveAt);
    }

    


    private void trimIfNeeded() {
        while (estimatedTokens > maxTokens && messages.size() > 2) {
            
            ChatRequest.Message removed = messages.remove(0);
            estimatedTokens -= estimateTokens(removed.getContent());
        }
    }

    


    private int estimateTokens(String text) {
        if (text == null) return 0;
        
        return text.length() / 3 + 1;
    }
}