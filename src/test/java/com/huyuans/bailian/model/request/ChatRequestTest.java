package com.huyuans.bailian.model.request;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChatRequest model.
 */
class ChatRequestTest {

    @Test
    void testCreateChatRequest() {
        ChatRequest request = ChatRequest.builder()
                .model("qwen-turbo")
                .messages(Arrays.asList(ChatRequest.Message.user("Hello")))
                .temperature(0.7)
                .topP(0.9)
                .maxTokens(1000)
                .build();

        assertEquals("qwen-turbo", request.getModel());
        assertEquals(1, request.getMessages().size());
        assertEquals(0.7, request.getTemperature());
        assertEquals(0.9, request.getTopP());
        assertEquals(1000, request.getMaxTokens());
    }

    @Test
    void testStreamValue() {
        ChatRequest request1 = ChatRequest.builder().stream(true).build();
        assertTrue(request1.getStream());

        ChatRequest request2 = ChatRequest.builder().stream(false).build();
        assertFalse(request2.getStream());
    }

    @Test
    void testExtraParameters() {
        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("key1", "value1");
        extraParams.put("key2", 123);

        ChatRequest request = ChatRequest.builder()
                .extraParameters(extraParams)
                .build();

        assertNotNull(request.getExtraParameters());
        assertEquals(2, request.getExtraParameters().size());
        assertEquals("value1", request.getExtraParameters().get("key1"));
        assertEquals(123, request.getExtraParameters().get("key2"));
    }

    @Test
    void testMessageFactoryMethods() {
        ChatRequest.Message userMsg = ChatRequest.Message.user("Hello user");
        assertEquals("user", userMsg.getRole());
        assertEquals("Hello user", userMsg.getContent());

        ChatRequest.Message systemMsg = ChatRequest.Message.system("You are helpful");
        assertEquals("system", systemMsg.getRole());
        assertEquals("You are helpful", systemMsg.getContent());

        ChatRequest.Message assistantMsg = ChatRequest.Message.assistant("Hello assistant");
        assertEquals("assistant", assistantMsg.getRole());
        assertEquals("Hello assistant", assistantMsg.getContent());
    }
}