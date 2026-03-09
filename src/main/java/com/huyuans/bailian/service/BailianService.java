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

/**
 * 百炼模型服务层
 */
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

    /**
     * 简单聊天（单轮对话）
     *
     * @param message 用户消息
     * @return 聊天响应
     */
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

    /**
     * 带系统提示的聊天
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return 聊天响应
     */
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

    /**
     * 聊天（可配置参数）
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
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

    /**
     * 流式聊天（简单版）
     *
     * @param message   用户消息
     * @param consumer  流式响应消费者
     */
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

    /**
     * 流式聊天（完整版）
     *
     * @param request   聊天请求
     * @param consumer  流式响应消费者
     */
    public void chatStream(ChatRequest request, Consumer<ChatStreamResponse> consumer) {
        if (request.getModel() == null) {
            request.setModel(properties.getDefaultModel());
        }
        bailianClient.chatStream(request)
                .doOnNext(consumer)
                .subscribe();
    }

    /**
     * 流式聊天返回Flux（可自定义订阅）
     *
     * @param request 聊天请求
     * @return Flux流
     */
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

    /**
     * 流式聊天返回Flux（简单版）
     *
     * @param message 用户消息
     * @return Flux流
     */
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

    /**
     * 简单Embedding（单条文本，带缓存）
     *
     * @param text 文本
     * @return Embedding响应
     */
    public Mono<EmbeddingResponse> embedding(String text) {
        return embedding(Collections.singletonList(text));
    }

    /**
     * Embedding（多条文本，带缓存）
     *
     * @param texts 文本列表
     * @return Embedding响应
     */
    public Mono<EmbeddingResponse> embedding(List<String> texts) {
        String model = properties.getDefaultEmbeddingModel();
        String cacheKey = embeddingCache.generateKey(model, texts);

        // 尝试从缓存获取
        Optional<EmbeddingResponse> cached = embeddingCache.get(cacheKey);
        if (cached.isPresent()) {
            metricsRecorder.recordEmbeddingCacheHit();
            return Mono.just(cached.get());
        }

        metricsRecorder.recordEmbeddingCacheMiss();
        final long startTime = System.currentTimeMillis();
        final int textCount = texts.size();

        // 缓存未命中，调用API
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

    /**
     * Embedding（自定义请求）
     *
     * @param request Embedding请求
     * @return Embedding响应
     */
    public Mono<EmbeddingResponse> embedding(EmbeddingRequest request) {
        if (request.getModel() == null) {
            request.setModel(properties.getDefaultEmbeddingModel());
        }
        return bailianClient.embedding(request);
    }

    /**
     * 获取Embedding缓存
     *
     * @return Embedding缓存实例
     */
    public EmbeddingCache getEmbeddingCache() {
        return embeddingCache;
    }

    // ==================== 新增便捷方法 ====================

    /**
     * 多轮对话
     * <p>
     * 使用示例：
     * <pre>
     * List<ChatRequest.Message> history = new ArrayList<>();
     * 
     * // 第一轮
     * String reply1 = bailianService.multiTurnChat(history, "你好").block();
     * 
     * // 第二轮（历史已自动更新）
     * String reply2 = bailianService.multiTurnChat(history, "介绍一下你自己").block();
     * </pre>
     *
     * @param history  对话历史（会被修改）
     * @param userMessage 用户消息
     * @return 助手回复内容
     */
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

    /**
     * 多轮对话（带系统提示）
     *
     * @param systemPrompt 系统提示词
     * @param history      对话历史（会被修改）
     * @param userMessage  用户消息
     * @return 助手回复内容
     */
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

    /**
     * 流式聊天并收集完整文本
     * <p>
     * 适合需要完整结果但不阻塞的场景
     *
     * @param message 用户消息
     * @return 完整回复文本
     */
    public Mono<String> chatStreamCollect(String message) {
        return StreamUtils.collectText(chatStreamFlux(message));
    }

    /**
     * 流式聊天并收集完整结果（包含元信息）
     *
     * @param message 用户消息
     * @return 流式结果（包含完整文本和元信息）
     */
    public Mono<StreamUtils.StreamResult> chatStreamResult(String message) {
        return StreamUtils.collectResult(chatStreamFlux(message));
    }

    /**
     * 流式聊天并收集完整文本（带参数）
     *
     * @param request 聊天请求
     * @return 完整回复文本
     */
    public Mono<String> chatStreamCollect(ChatRequest request) {
        return StreamUtils.collectText(chatStreamFlux(request));
    }

    /**
     * 使用指定模型聊天
     *
     * @param model   模型名称
     * @param message 用户消息
     * @return 聊天响应
     */
    public Mono<ChatResponse> chatWithModel(String model, String message) {
        return chat(ChatRequest.builder()
                .model(model)
                .messages(Collections.singletonList(
                        ChatRequest.Message.user(message)
                ))
                .build());
    }

    /**
     * 获取回复内容（便捷方法）
     *
     * @param message 用户消息
     * @return 回复内容字符串
     */
    public Mono<String> chatContent(String message) {
        return chat(message)
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        return response.getChoices().get(0).getMessage().getContent();
                    }
                    return "";
                });
    }

    /**
     * 获取回复内容（带系统提示）
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return 回复内容字符串
     */
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