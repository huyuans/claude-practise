package com.huyuans.bailian.util;

import com.huyuans.bailian.model.request.ChatRequest;

import java.util.ArrayList;
import java.util.List;




























public class ChatRequestBuilder {

    
    private final List<ChatRequest.Message> messages = new ArrayList<>();
    
    
    private String model;
    
    
    private Double temperature;
    
    
    private Double topP;
    
    
    private Integer maxTokens;

    private ChatRequestBuilder() {}

    


    public static ChatRequestBuilder create() {
        return new ChatRequestBuilder();
    }

    


    public static ChatRequestBuilder create(String model) {
        return new ChatRequestBuilder().model(model);
    }

    


    public ChatRequestBuilder model(String model) {
        this.model = model;
        return this;
    }

    


    public ChatRequestBuilder temperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    


    public ChatRequestBuilder topP(Double topP) {
        this.topP = topP;
        return this;
    }

    


    public ChatRequestBuilder maxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    


    public ChatRequestBuilder system(String content) {
        messages.add(ChatRequest.Message.system(content));
        return this;
    }

    


    public ChatRequestBuilder user(String content) {
        messages.add(ChatRequest.Message.user(content));
        return this;
    }

    


    public ChatRequestBuilder assistant(String content) {
        messages.add(ChatRequest.Message.assistant(content));
        return this;
    }

    


    public ChatRequestBuilder message(String role, String content) {
        messages.add(ChatRequest.Message.builder().role(role).content(content).build());
        return this;
    }

    


    public ChatRequest build() {
        return ChatRequest.builder()
                .model(model)
                .messages(new ArrayList<>(messages))
                .temperature(temperature)
                .topP(topP)
                .maxTokens(maxTokens)
                .build();
    }
}