package com.huyuans.bailian.client;

import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.model.request.ChatRequest;
import com.huyuans.bailian.model.request.EmbeddingRequest;
import com.huyuans.bailian.model.response.ChatResponse;
import com.huyuans.bailian.model.response.EmbeddingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BailianClient and related components.
 */
class BailianClientTest {

    private BailianProperties properties;

    @BeforeEach
    void setUp() {
        properties = new BailianProperties();
        properties.setApiKey("test-api-key");
        properties.getRetry().setEnabled(false);
        properties.getConnectionPool().setEnabled(false);
    }

    @Test
    void testClientInitialization() {
        assertNotNull(new BailianClient(properties));
    }

    @Test
    void testBuildChatRequest() {
        ChatRequest request = ChatRequest.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(ChatRequest.Message.user("Hello")))
                .temperature(0.7)
                .maxTokens(1000)
                .build();

        assertEquals("qwen-turbo", request.getModel());
        assertEquals(1, request.getMessages().size());
        assertEquals(0.7, request.getTemperature());
    }

    @Test
    void testBuildEmbeddingRequest() {
        EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-v3")
                .input(Arrays.asList("test text"))
                .build();

        assertEquals("text-embedding-v3", request.getModel());
        assertEquals(1, request.getInput().size());
    }

    @Test
    void testCreateChatResponse() {
        ChatResponse response = ChatResponse.builder()
                .id("test-id")
                .model("qwen-turbo")
                .choices(Arrays.asList(ChatResponse.Choice.builder()
                        .index(0)
                        .message(ChatResponse.Message.builder().role("assistant").content("Hello!").build())
                        .finishReason("stop")
                        .build()))
                .usage(ChatResponse.Usage.builder().promptTokens(10).completionTokens(5).totalTokens(15).build())
                .build();

        assertEquals("test-id", response.getId());
        assertEquals("Hello!", response.getChoices().get(0).getMessage().getContent());
    }

    @Test
    void testCreateEmbeddingResponse() {
        EmbeddingResponse response = EmbeddingResponse.builder()
                .id("emb-id")
                .model("text-embedding-v3")
                .embeddings(Arrays.asList(EmbeddingResponse.Embedding.builder()
                        .embedding(Arrays.asList(0.1f, 0.2f, 0.3f))
                        .index(0)
                        .build()))
                .build();

        assertEquals("emb-id", response.getId());
        assertEquals(1, response.getEmbeddings().size());
    }

    @Test
    void testBailianException() {
        BailianException ex = new BailianException("Test error", 500, "Internal Error");
        assertEquals("Test error", ex.getMessage());
        assertEquals(500, ex.getHttpStatus());
        assertEquals("Internal Error", ex.getResponseBody());
    }

    @Test
    void testMessageStaticMethods() {
        assertEquals("user", ChatRequest.Message.user("Hello").getRole());
        assertEquals("system", ChatRequest.Message.system("System").getRole());
        assertEquals("assistant", ChatRequest.Message.assistant("Hi").getRole());
    }
}
