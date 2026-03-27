package com.huyuans.bailian.service;

import com.huyuans.bailian.cache.EmbeddingCache;
import com.huyuans.bailian.client.BailianClient;
import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.metrics.BailianMetricsRecorder;
import com.huyuans.bailian.model.request.ChatRequest;
import com.huyuans.bailian.model.response.ChatResponse;
import com.huyuans.bailian.model.response.ChatStreamResponse;
import com.huyuans.bailian.model.response.EmbeddingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BailianService.
 */
@ExtendWith(MockitoExtension.class)
class BailianServiceTest {

    @Mock private BailianClient bailianClient;
    @Mock private EmbeddingCache embeddingCache;
    @Mock private BailianMetricsRecorder metricsRecorder;

    private BailianService service;

    @BeforeEach
    void setUp() {
        BailianProperties properties = new BailianProperties();
        properties.setDefaultModel("qwen-turbo");
        properties.setDefaultEmbeddingModel("text-embedding-v3");
        service = new BailianService(bailianClient, properties, embeddingCache, metricsRecorder);
    }

    @Test
    void testSimpleChat() {
        when(bailianClient.chat(any())).thenReturn(Mono.just(createChatResponse("Hello!")));

        StepVerifier.create(service.chat("Hi"))
                .assertNext(r -> assertEquals("Hello!", r.getChoices().get(0).getMessage().getContent()))
                .verifyComplete();
    }

    @Test
    void testChatWithSystem() {
        when(bailianClient.chat(any())).thenReturn(Mono.just(createChatResponse("Response")));

        StepVerifier.create(service.chatWithSystem("You are helpful", "Hi"))
                .assertNext(r -> assertNotNull(r))
                .verifyComplete();
    }

    @Test
    void testChatContent() {
        when(bailianClient.chat(any())).thenReturn(Mono.just(createChatResponse("Test content")));

        StepVerifier.create(service.chatContent("Hi"))
                .expectNext("Test content")
                .verifyComplete();
    }

    @Test
    void testMultiTurnChat() {
        when(bailianClient.chat(any()))
                .thenReturn(Mono.just(createChatResponse("Reply 1")))
                .thenReturn(Mono.just(createChatResponse("Reply 2")));

        List<ChatRequest.Message> history = new ArrayList<>();

        StepVerifier.create(service.multiTurnChat(history, "Q1")).expectNext("Reply 1").verifyComplete();
        StepVerifier.create(service.multiTurnChat(history, "Q2")).expectNext("Reply 2").verifyComplete();

        assertEquals(4, history.size());
    }

    @Test
    void testChatStreamFlux() {
        when(bailianClient.chatStream(any()))
                .thenReturn(Flux.just(createStreamResponse("Hello"), createStreamResponse(" World")));

        StepVerifier.create(service.chatStreamFlux("Hi"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void testChatStreamCollect() {
        when(bailianClient.chatStream(any()))
                .thenReturn(Flux.just(createStreamResponse("A"), createStreamResponse("B")));

        StepVerifier.create(service.chatStreamCollect("Hi"))
                .expectNext("AB")
                .verifyComplete();
    }

    @Test
    void testEmbeddingCacheMiss() {
        when(embeddingCache.generateKey(anyString(), any())).thenReturn("key");
        when(embeddingCache.get("key")).thenReturn(Optional.empty());
        when(bailianClient.embedding(any())).thenReturn(Mono.just(createEmbeddingResponse()));

        StepVerifier.create(service.embedding("test"))
                .assertNext(r -> assertNotNull(r))
                .verifyComplete();

        verify(embeddingCache).put(anyString(), any());
    }

    @Test
    void testEmbeddingCacheHit() {
        when(embeddingCache.generateKey(anyString(), any())).thenReturn("key");
        when(embeddingCache.get("key")).thenReturn(Optional.of(createEmbeddingResponse()));

        StepVerifier.create(service.embedding("test"))
                .assertNext(r -> assertNotNull(r))
                .verifyComplete();

        verify(bailianClient, never()).embedding(any());
    }

    private ChatResponse createChatResponse(String content) {
        return ChatResponse.builder()
                .id("test-id")
                .model("qwen-turbo")
                .choices(Arrays.asList(ChatResponse.Choice.builder()
                        .index(0)
                        .message(ChatResponse.Message.builder().role("assistant").content(content).build())
                        .finishReason("stop")
                        .build()))
                .usage(ChatResponse.Usage.builder().promptTokens(10).completionTokens(20).totalTokens(30).build())
                .build();
    }

    private ChatStreamResponse createStreamResponse(String content) {
        ChatStreamResponse.Delta delta = new ChatStreamResponse.Delta();
        delta.setContent(content);
        ChatStreamResponse.Choice choice = new ChatStreamResponse.Choice();
        choice.setDelta(delta);
        ChatStreamResponse response = new ChatStreamResponse();
        response.setChoices(Arrays.asList(choice));
        return response;
    }

    private EmbeddingResponse createEmbeddingResponse() {
        return EmbeddingResponse.builder()
                .id("emb-id")
                .model("text-embedding-v3")
                .embeddings(Arrays.asList(EmbeddingResponse.Embedding.builder()
                        .embedding(Arrays.asList(0.1f, 0.2f, 0.3f)).index(0).build()))
                .build();
    }
}
