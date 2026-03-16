package com.huyuans.bailian.model.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatRequest 单元测试
 * <p>
 * 测试聊天请求模型的功能，包括：
 * <ul>
 *   <li>请求参数的构建和读取</li>
 *   <li>消息静态工厂方法</li>
 *   <li>额外参数的处理</li>
 * </ul>
 *
 * @author Kasper
 * @since 1.0.0
 */
@DisplayName("聊天请求模型测试")
class ChatRequestTest {

    @Test
    @DisplayName("创建聊天请求")
    void testCreateChatRequest() {
        ChatRequest request = ChatRequest.builder()
                .model("qwen-turbo")
                .messages(List.of(
                        ChatRequest.Message.user("Hello")
                ))
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
    @DisplayName("stream字段测试")
    void testStreamValue() {
        ChatRequest request = ChatRequest.builder()
                .stream(true)
                .build();
        assertTrue(request.getStream());
        
        ChatRequest request2 = ChatRequest.builder()
                .stream(false)
                .build();
        assertFalse(request2.getStream());
    }

    @Test
    @DisplayName("创建用户消息")
    void testCreateUserMessage() {
        ChatRequest.Message msg = ChatRequest.Message.user("Test message");
        
        assertEquals("user", msg.getRole());
        assertEquals("Test message", msg.getContent());
    }

    @Test
    @DisplayName("创建系统消息")
    void testCreateSystemMessage() {
        ChatRequest.Message msg = ChatRequest.Message.system("System prompt");
        
        assertEquals("system", msg.getRole());
        assertEquals("System prompt", msg.getContent());
    }

    @Test
    @DisplayName("创建助手消息")
    void testCreateAssistantMessage() {
        ChatRequest.Message msg = ChatRequest.Message.assistant("Assistant reply");
        
        assertEquals("assistant", msg.getRole());
        assertEquals("Assistant reply", msg.getContent());
    }

    @Test
    @DisplayName("消息Builder测试")
    void testMessageBuilder() {
        ChatRequest.Message msg = ChatRequest.Message.builder()
                .role("user")
                .content("Hello")
                .build();

        assertEquals("user", msg.getRole());
        assertEquals("Hello", msg.getContent());
    }

    @Test
    @DisplayName("消息Setter测试")
    void testMessageSetter() {
        ChatRequest.Message msg = new ChatRequest.Message();
        msg.setRole("assistant");
        msg.setContent("Reply");

        assertEquals("assistant", msg.getRole());
        assertEquals("Reply", msg.getContent());
    }

    @Test
    @DisplayName("请求Setter测试")
    void testRequestSetter() {
        ChatRequest request = new ChatRequest();
        request.setModel("qwen-max");
        request.setTemperature(0.5);
        request.setTopP(0.8);
        request.setMaxTokens(500);
        request.setStream(true);

        assertEquals("qwen-max", request.getModel());
        assertEquals(0.5, request.getTemperature());
        assertEquals(0.8, request.getTopP());
        assertEquals(500, request.getMaxTokens());
        assertTrue(request.getStream());
    }

    @Test
    @DisplayName("额外参数测试")
    void testExtraParameters() {
        ChatRequest request = ChatRequest.builder()
                .extraParameters(Map.of("key1", "value1", "key2", 123))
                .build();

        assertNotNull(request.getExtraParameters());
        assertEquals("value1", request.getExtraParameters().get("key1"));
        assertEquals(123, request.getExtraParameters().get("key2"));
    }
}