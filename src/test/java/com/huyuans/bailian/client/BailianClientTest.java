package com.huyuans.bailian.client;

import com.huyuans.bailian.config.BailianProperties;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;















@ExtendWith(MockitoExtension.class)
@DisplayName("百炼客户端测试")
class BailianClientTest {

    
    private BailianClient client;
    
    
    private BailianProperties properties;

    @BeforeEach
    void setUp() {
        properties = new BailianProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://dashscope.aliyuncs.com");
        properties.setDefaultModel("qwen-turbo");
        properties.setDefaultEmbeddingModel("text-embedding-v3");
        properties.setTimeout(60000);
        
        
        BailianProperties.RetryConfig retryConfig = new BailianProperties.RetryConfig();
        retryConfig.setEnabled(false); 
        properties.setRetry(retryConfig);
        
        
        BailianProperties.ConnectionPoolConfig poolConfig = new BailianProperties.ConnectionPoolConfig();
        poolConfig.setEnabled(false);
        properties.setConnectionPool(poolConfig);

        client = new BailianClient(properties);
    }

    @Test
    @DisplayName("客户端初始化成功")
    void testClientInitialization() {
        assertNotNull(client);
    }

    @Test
    @DisplayName("构建聊天请求体")
    void testBuildChatRequest() {
        ChatRequest request = ChatRequest.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(
                        ChatRequest.Message.user("Hello")
                ))
                .temperature(0.7)
                .maxTokens(1000)
                .build();

        assertNotNull(request);
        assertEquals("qwen-turbo", request.getModel());
        assertEquals(1, request.getMessages().size());
    }

    @Test
    @DisplayName("构建流式聊天请求体")
    void testBuildStreamChatRequest() {
        ChatRequest request = ChatRequest.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(
                        ChatRequest.Message.user("Hello")
                ))
                .build();

        assertNotNull(request);
        assertNotNull(request.getMessages());
    }

    @Test
    @DisplayName("构建Embedding请求体")
    void testBuildEmbeddingRequest() {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-v3")
                .input(Arrays.asList("test text"))
                .build();

        assertNotNull(request);
        assertEquals("text-embedding-v3", request.getModel());
        assertEquals(1, request.getInput().size());
    }

    @Test
    @DisplayName("创建聊天响应")
    void testCreateChatResponse() {
        ChatResponse response = ChatResponse.builder()
                .id("test-id")
                .model("qwen-turbo")
                .choices(Arrays.asList(
                        ChatResponse.Choice.builder()
                                .index(0)
                                .message(ChatResponse.Message.builder()
                                        .role("assistant")
                                        .content("Hello!")
                                        .build())
                                .finishReason("stop")
                                .build()
                ))
                .usage(ChatResponse.Usage.builder()
                        .promptTokens(10)
                        .completionTokens(5)
                        .totalTokens(15)
                        .build())
                .build();

        assertNotNull(response);
        assertEquals("test-id", response.getId());
        assertEquals(1, response.getChoices().size());
        assertEquals("Hello!", response.getChoices().get(0).getMessage().getContent());
    }

    @Test
    @DisplayName("创建Embedding响应")
    void testCreateEmbeddingResponse() {
        EmbeddingResponse response = EmbeddingResponse.builder()
                .id("emb-id")
                .model("text-embedding-v3")
                .embeddings(Arrays.asList(
                        EmbeddingResponse.Embedding.builder()
                                .embedding(Arrays.asList(0.1f, 0.2f, 0.3f))
                                .index(0)
                                .build()
                ))
                .usage(EmbeddingResponse.Usage.builder()
                        .promptTokens(5)
                        .totalTokens(5)
                        .build())
                .build();

        assertNotNull(response);
        assertEquals("emb-id", response.getId());
        assertEquals(1, response.getEmbeddings().size());
    }

    @Test
    @DisplayName("创建流式响应")
    void testCreateStreamResponse() {
        ChatStreamResponse response = new ChatStreamResponse();
        response.setId("stream-id");
        response.setModel("qwen-turbo");

        ChatStreamResponse.Delta delta = new ChatStreamResponse.Delta();
        delta.setContent("Hello");
        delta.setRole("assistant");

        ChatStreamResponse.Choice choice = new ChatStreamResponse.Choice();
        choice.setDelta(delta);
        choice.setFinishReason(null);

        response.setChoices(Arrays.asList(choice));

        assertNotNull(response);
        assertEquals("stream-id", response.getId());
        assertEquals("Hello", response.getChoices().get(0).getDelta().getContent());
    }

    @Test
    @DisplayName("BailianException创建")
    void testBailianException() {
        BailianException ex1 = new BailianException("Test error");
        assertEquals("Test error", ex1.getMessage());

        BailianException ex2 = new BailianException("Test error", 500, "Internal Error");
        assertEquals("Test error", ex2.getMessage());

        BailianException ex3 = new BailianException("Test error", new RuntimeException("Cause"));
        assertEquals("Test error", ex3.getMessage());
        assertNotNull(ex3.getCause());
    }

    @Test
    @DisplayName("Message静态方法")
    void testMessageStaticMethods() {
        ChatRequest.Message userMsg = ChatRequest.Message.user("Hello");
        assertEquals("user", userMsg.getRole());
        assertEquals("Hello", userMsg.getContent());

        ChatRequest.Message systemMsg = ChatRequest.Message.system("System prompt");
        assertEquals("system", systemMsg.getRole());

        ChatRequest.Message assistantMsg = ChatRequest.Message.assistant("Hi there");
        assertEquals("assistant", assistantMsg.getRole());
    }

    @Test
    @DisplayName("ChatResponse.Usage测试")
    void testChatResponseUsage() {
        ChatResponse.Usage usage = ChatResponse.Usage.builder()
                .promptTokens(100)
                .completionTokens(50)
                .totalTokens(150)
                .build();

        assertEquals(100, usage.getPromptTokens());
        assertEquals(50, usage.getCompletionTokens());
        assertEquals(150, usage.getTotalTokens());
    }

    @Test
    @DisplayName("EmbeddingResponse.Usage测试")
    void testEmbeddingResponseUsage() {
        EmbeddingResponse.Usage usage = EmbeddingResponse.Usage.builder()
                .promptTokens(100)
                .totalTokens(100)
                .build();

        assertEquals(100, usage.getPromptTokens());
        assertEquals(100, usage.getTotalTokens());
    }
}