package com.huyuans.bailian.service;

import com.huyuans.bailian.cache.EmbeddingCache;
import com.huyuans.bailian.client.BailianClient;
import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.metrics.BailianMetricsRecorder;
import com.huyuans.bailian.model.request.ChatRequest;
import com.huyuans.bailian.model.request.EmbeddingRequest;
import com.huyuans.bailian.model.response.ChatResponse;
import com.huyuans.bailian.model.response.ChatStreamResponse;
import com.huyuans.bailian.model.response.EmbeddingResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.huyuans.bailian.util.StreamUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;



































@Slf4j
public class BailianService {

    
    private final BailianClient bailianClient;
    
    
    private final BailianProperties properties;
    
    
    private final EmbeddingCache embeddingCache;
    
    
    private final BailianMetricsRecorder metricsRecorder;

    public BailianService(BailianClient bailianClient, BailianProperties properties, 
                          EmbeddingCache embeddingCache, BailianMetricsRecorder metricsRecorder) {
        this.bailianClient = bailianClient;
        this.properties = properties;
        this.embeddingCache = embeddingCache;
        this.metricsRecorder = metricsRecorder;
    }

    





    public Mono<ChatResponse> chat(String message) {
        return chat(ChatRequest.builder()
                .model(properties.getDefaultModel())
                .messages(Collections.singletonList(
                        ChatRequest.Message.builder()
                                .role("user")
                                .content(message)
                                .build()
                ))
                .build());
    }

    






    public Mono<ChatResponse> chatWithSystem(String systemPrompt, String userMessage) {
        return chat(ChatRequest.builder()
                .model(properties.getDefaultModel())
                .messages(List.of(
                        ChatRequest.Message.builder()
                                .role("system")
                                .content(systemPrompt)
                                .build(),
                        ChatRequest.Message.builder()
                                .role("user")
                                .content(userMessage)
                                .build()
                ))
                .build());
    }

    








    public Mono<ChatResponse> chat(ChatRequest request) {
        
        if (request.getModel() == null) {
            request.setModel(properties.getDefaultModel());
        }
        final String model = request.getModel();
        final long startTime = System.currentTimeMillis();
        
        return bailianClient.chat(request)
                
                .doOnSuccess(response -> {
                    long duration = System.currentTimeMillis() - startTime;
                    Integer tokens = response.getUsage() != null ? 
                            response.getUsage().getTotalTokens() : null;
                    metricsRecorder.recordChatRequest(model, true, duration, tokens != null ? tokens.longValue() : null);
                })
                
                .doOnError(e -> {
                    long duration = System.currentTimeMillis() - startTime;
                    metricsRecorder.recordChatRequest(model, false, duration, null);
                });
    }

    





    public void chatStream(String message, Consumer<ChatStreamResponse> consumer) {
        chatStream(ChatRequest.builder()
                .model(properties.getDefaultModel())
                .messages(Collections.singletonList(
                        ChatRequest.Message.builder()
                                .role("user")
                                .content(message)
                                .build()
                ))
                .build(), consumer);
    }

    





    public void chatStream(ChatRequest request, Consumer<ChatStreamResponse> consumer) {
        if (request.getModel() == null) {
            request.setModel(properties.getDefaultModel());
        }
        bailianClient.chatStream(request)
                .doOnNext(consumer)
                .subscribe();
    }

    





    public Flux<ChatStreamResponse> chatStreamFlux(ChatRequest request) {
        if (request.getModel() == null) {
            request.setModel(properties.getDefaultModel());
        }
        final String model = request.getModel();
        final long startTime = System.currentTimeMillis();
        
        return bailianClient.chatStream(request)
                .doOnComplete(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    metricsRecorder.recordStreamRequest(model, true, duration);
                })
                .doOnError(e -> {
                    long duration = System.currentTimeMillis() - startTime;
                    metricsRecorder.recordStreamRequest(model, false, duration);
                });
    }

    





    public Flux<ChatStreamResponse> chatStreamFlux(String message) {
        return chatStreamFlux(ChatRequest.builder()
                .model(properties.getDefaultModel())
                .messages(Collections.singletonList(
                        ChatRequest.Message.builder()
                                .role("user")
                                .content(message)
                                .build()
                ))
                .build());
    }

    





    public Mono<EmbeddingResponse> embedding(String text) {
        return embedding(Collections.singletonList(text));
    }

    








    public Mono<EmbeddingResponse> embedding(List<String> texts) {
        String model = properties.getDefaultEmbeddingModel();
        String cacheKey = embeddingCache.generateKey(model, texts);

        
        Optional<EmbeddingResponse> cached = embeddingCache.get(cacheKey);
        if (cached.isPresent()) {
            metricsRecorder.recordEmbeddingCacheHit();
            return Mono.just(cached.get());
        }

        metricsRecorder.recordEmbeddingCacheMiss();
        final long startTime = System.currentTimeMillis();
        final int textCount = texts.size();

        
        return embedding(EmbeddingRequest.builder()
                .model(model)
                .input(texts)
                .build())
                .doOnNext(response -> {
                    embeddingCache.put(cacheKey, response);
                    long duration = System.currentTimeMillis() - startTime;
                    metricsRecorder.recordEmbeddingRequest(model, true, duration, textCount);
                })
                .doOnError(e -> {
                    long duration = System.currentTimeMillis() - startTime;
                    metricsRecorder.recordEmbeddingRequest(model, false, duration, textCount);
                });
    }

    





    public Mono<EmbeddingResponse> embedding(EmbeddingRequest request) {
        if (request.getModel() == null) {
            request.setModel(properties.getDefaultEmbeddingModel());
        }
        return bailianClient.embedding(request);
    }

    




    public EmbeddingCache getEmbeddingCache() {
        return embeddingCache;
    }

    

    

















    public Mono<String> multiTurnChat(List<ChatRequest.Message> history, String userMessage) {
        history.add(ChatRequest.Message.user(userMessage));

        return chat(ChatRequest.builder()
                .model(properties.getDefaultModel())
                .messages(new ArrayList<>(history))
                .build())
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        String content = response.getChoices().get(0).getMessage().getContent();
                        history.add(ChatRequest.Message.assistant(content));
                        return content;
                    }
                    return "";
                });
    }

    







    public Mono<String> multiTurnChat(String systemPrompt, List<ChatRequest.Message> history, String userMessage) {
        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(ChatRequest.Message.system(systemPrompt));
        messages.addAll(history);
        messages.add(ChatRequest.Message.user(userMessage));

        return chat(ChatRequest.builder()
                .model(properties.getDefaultModel())
                .messages(messages)
                .build())
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        String content = response.getChoices().get(0).getMessage().getContent();
                        history.add(ChatRequest.Message.user(userMessage));
                        history.add(ChatRequest.Message.assistant(content));
                        return content;
                    }
                    return "";
                });
    }

    







    public Mono<String> chatStreamCollect(String message) {
        return StreamUtils.collectText(chatStreamFlux(message));
    }

    





    public Mono<StreamUtils.StreamResult> chatStreamResult(String message) {
        return StreamUtils.collectResult(chatStreamFlux(message));
    }

    





    public Mono<String> chatStreamCollect(ChatRequest request) {
        return StreamUtils.collectText(chatStreamFlux(request));
    }

    






    public Mono<ChatResponse> chatWithModel(String model, String message) {
        return chat(ChatRequest.builder()
                .model(model)
                .messages(Collections.singletonList(
                        ChatRequest.Message.user(message)
                ))
                .build());
    }

    





    public Mono<String> chatContent(String message) {
        return chat(message)
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        return response.getChoices().get(0).getMessage().getContent();
                    }
                    return "";
                });
    }

    






    public Mono<String> chatContent(String systemPrompt, String userMessage) {
        return chatWithSystem(systemPrompt, userMessage)
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        return response.getChoices().get(0).getMessage().getContent();
                    }
                    return "";
                });
    }
}