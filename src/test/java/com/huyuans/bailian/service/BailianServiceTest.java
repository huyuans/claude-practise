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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BailianService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("百炼服务测试")
class BailianServiceTest {

    @Mock
    private BailianClient bailianClient;

    @Mock
    private EmbeddingCache embeddingCache;

    @Mock
    private BailianMetricsRecorder metricsRecorder;

    private BailianService service;
    private BailianProperties properties;

    @BeforeEach
    void setUp() {
        properties = new BailianProperties();
        properties.setDefaultModel("qwen-turbo");
        properties.setDefaultEmbeddingModel("text-embedding-v3");
        service = new BailianService(bailianClient, properties, embeddingCache, metricsRecorder);
    }

    @Test
    @DisplayName("简单聊天")
    void testSimpleChat() {
        ChatResponse mockResponse = createChatResponse("Hello!");
        when(bailianClient.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        StepVerifier.create(service.chat("Hi"))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals("Hello!", response.getChoices().get(0).getMessage().getContent());
                })
                .verifyComplete();

        verify(bailianClient).chat(any(ChatRequest.class));
    }

    @Test
    @DisplayName("带系统提示的聊天")
    void testChatWithSystem() {
        ChatResponse mockResponse = createChatResponse("Response");
        when(bailianClient.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        StepVerifier.create(service.chatWithSystem("You are helpful", "Hi"))
                .assertNext(response -> assertNotNull(response))
                .verifyComplete();
    }

    @Test
    @DisplayName("聊天内容提取")
    void testChatContent() {
        ChatResponse mockResponse = createChatResponse("Test content");
        when(bailianClient.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        StepVerifier.create(service.chatContent("Hi"))
                .expectNext("Test content")
                .verifyComplete();
    }

    @Test
    @DisplayName("使用指定模型聊天")
    void testChatWithModel() {
        ChatResponse mockResponse = createChatResponse("Response");
        when(bailianClient.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        StepVerifier.create(service.chatWithModel("qwen-max", "Hi"))
                .assertNext(response -> assertNotNull(response))
                .verifyComplete();
    }

    @Test
    @DisplayName("多轮对话")
    void testMultiTurnChat() {
        ChatResponse mockResponse1 = createChatResponse("Reply 1");
        ChatResponse mockResponse2 = createChatResponse("Reply 2");
        when(bailianClient.chat(any(ChatRequest.class)))
                .thenReturn(Mono.just(mockResponse1))
                .thenReturn(Mono.just(mockResponse2));

        List<ChatRequest.Message> history = new ArrayList<>();

        StepVerifier.create(service.multiTurnChat(history, "Q1"))
                .expectNext("Reply 1")
                .verifyComplete();

        assertEquals(2, history.size());

        StepVerifier.create(service.multiTurnChat(history, "Q2"))
                .expectNext("Reply 2")
                .verifyComplete();

        assertEquals(4, history.size());
    }

    @Test
    @DisplayName("流式聊天返回Flux")
    void testChatStreamFlux() {
        ChatStreamResponse response1 = createStreamResponse("Hello");
        ChatStreamResponse response2 = createStreamResponse(" World");
        when(bailianClient.chatStream(any(ChatRequest.class)))
                .thenReturn(Flux.just(response1, response2));

        StepVerifier.create(service.chatStreamFlux("Hi"))
                .expectNext(response1)
                .expectNext(response2)
                .verifyComplete();
    }

    @Test
    @DisplayName("流式聊天收集文本")
    void testChatStreamCollect() {
        ChatStreamResponse response1 = createStreamResponse("A");
        ChatStreamResponse response2 = createStreamResponse("B");
        when(bailianClient.chatStream(any(ChatRequest.class)))
                .thenReturn(Flux.just(response1, response2));

        StepVerifier.create(service.chatStreamCollect("Hi"))
                .expectNext("AB")
                .verifyComplete();
    }

    @Test
    @DisplayName("简单Embedding")
    void testSimpleEmbedding() {
        EmbeddingResponse mockResponse = createEmbeddingResponse();
        when(embeddingCache.generateKey(anyString(), anyList())).thenReturn("test-key");
        when(embeddingCache.get("test-key")).thenReturn(Optional.empty());
        when(bailianClient.embedding(any(EmbeddingRequest.class))).thenReturn(Mono.just(mockResponse));

        StepVerifier.create(service.embedding("test text"))
                .assertNext(response -> assertNotNull(response))
                .verifyComplete();

        verify(embeddingCache).put(anyString(), any(EmbeddingResponse.class));
    }

    @Test
    @DisplayName("Embedding缓存命中")
    void testEmbeddingCacheHit() {
        EmbeddingResponse cachedResponse = createEmbeddingResponse();
        when(embeddingCache.generateKey(anyString(), anyList())).thenReturn("test-key");
        when(embeddingCache.get("test-key")).thenReturn(Optional.of(cachedResponse));

        StepVerifier.create(service.embedding("test"))
                .assertNext(response -> assertNotNull(response))
                .verifyComplete();

        verify(bailianClient, never()).embedding(any());
    }

    @Test
    @DisplayName("批量Embedding")
    void testBatchEmbedding() {
        EmbeddingResponse mockResponse = createEmbeddingResponse();
        when(embeddingCache.generateKey(anyString(), anyList())).thenReturn("test-key");
        when(embeddingCache.get("test-key")).thenReturn(Optional.empty());
        when(bailianClient.embedding(any(EmbeddingRequest.class))).thenReturn(Mono.just(mockResponse));

        List<String> texts = List.of("text1", "text2");
        StepVerifier.create(service.embedding(texts))
                .assertNext(response -> assertNotNull(response))
                .verifyComplete();
    }

    @Test
    @DisplayName("获取缓存实例")
    void testGetEmbeddingCache() {
        assertEquals(embeddingCache, service.getEmbeddingCache());
    }

    @Test
    @DisplayName("请求未指定模型时使用默认模型")
    void testDefaultModelApplied() {
        ChatResponse mockResponse = createChatResponse("OK");
        when(bailianClient.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        ChatRequest request = ChatRequest.builder()
                .messages(List.of(ChatRequest.Message.user("Hi")))
                .build();

        StepVerifier.create(service.chat(request))
                .assertNext(response -> {
                    assertEquals("qwen-turbo", request.getModel());
                })
                .verifyComplete();
    }

    // ==================== Helper Methods ====================

    private ChatResponse createChatResponse(String content) {
        ChatResponse.Message message = ChatResponse.Message.builder()
                .role("assistant")
                .content(content)
                .build();

        ChatResponse.Choice choice = ChatResponse.Choice.builder()
                .index(0)
                .message(message)
                .finishReason("stop")
                .build();

        return ChatResponse.builder()
                .id("test-id")
                .model("qwen-turbo")
                .choices(List.of(choice))
                .usage(ChatResponse.Usage.builder()
                        .promptTokens(10)
                        .completionTokens(20)
                        .totalTokens(30)
                        .build())
                .build();
    }

    private ChatStreamResponse createStreamResponse(String content) {
        ChatStreamResponse.Delta delta = new ChatStreamResponse.Delta();
        delta.setContent(content);

        ChatStreamResponse.Choice choice = new ChatStreamResponse.Choice();
        choice.setDelta(delta);

        ChatStreamResponse response = new ChatStreamResponse();
        response.setChoices(List.of(choice));
        return response;
    }

    private EmbeddingResponse createEmbeddingResponse() {
        EmbeddingResponse.Embedding embedding = EmbeddingResponse.Embedding.builder()
                .embedding(List.of(0.1f, 0.2f, 0.3f))
                .index(0)
                .build();

        return EmbeddingResponse.builder()
                .id("emb-id")
                .model("text-embedding-v3")
                .embeddings(List.of(embedding))
                .build();
    }
}