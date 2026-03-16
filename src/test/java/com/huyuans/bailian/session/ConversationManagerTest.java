package com.huyuans.bailian.session;

import com.huyuans.bailian.model.request.ChatRequest;
import com.huyuans.bailian.model.response.ChatResponse;
import com.huyuans.bailian.model.response.ChatResponse.Choice;
import com.huyuans.bailian.model.response.ChatResponse.Message;
import com.huyuans.bailian.service.BailianService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;















class ConversationManagerTest {

    
    @Mock
    private BailianService bailianService;

    
    private ConversationManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new ConversationManager(bailianService, 60, 4000);
    }

    @Test
    @DisplayName("创建会话")
    void testCreateSession() {
        String sessionId = manager.createSession();
        assertNotNull(sessionId);
        assertTrue(manager.getSession(sessionId).isPresent());
        assertEquals(1, manager.getActiveSessionCount());
    }

    @Test
    @DisplayName("创建带系统提示词的会话")
    void testCreateSessionWithSystemPrompt() {
        String sessionId = manager.createSession("你是一个助手");
        ConversationSession session = manager.getSession(sessionId).orElseThrow();
        assertEquals("你是一个助手", session.getSystemPrompt());
    }

    @Test
    @DisplayName("对话并自动管理历史")
    void testChat() {
        
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setChoices(List.of(
                new Choice(0, new Message("assistant", "你好！我是助手"), null)
        ));
        when(bailianService.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        String sessionId = manager.createSession();
        
        StepVerifier.create(manager.chat(sessionId, "你好"))
                .expectNext("你好！我是助手")
                .verifyComplete();

        
        List<ChatRequest.Message> history = manager.getHistory(sessionId).orElseThrow();
        assertEquals(2, history.size());
        assertEquals("user", history.get(0).getRole());
        assertEquals("你好", history.get(0).getContent());
        assertEquals("assistant", history.get(1).getRole());
        assertEquals("你好！我是助手", history.get(1).getContent());
    }

    @Test
    @DisplayName("多轮对话")
    void testMultiTurnChat() {
        
        ChatResponse response1 = new ChatResponse();
        response1.setChoices(List.of(new Choice(0, new Message("assistant", "回复1"), null)));
        
        ChatResponse response2 = new ChatResponse();
        response2.setChoices(List.of(new Choice(0, new Message("assistant", "回复2"), null)));
        
        when(bailianService.chat(any(ChatRequest.class)))
                .thenReturn(Mono.just(response1))
                .thenReturn(Mono.just(response2));

        String sessionId = manager.createSession();

        
        StepVerifier.create(manager.chat(sessionId, "问题1"))
                .expectNext("回复1")
                .verifyComplete();

        
        StepVerifier.create(manager.chat(sessionId, "问题2"))
                .expectNext("回复2")
                .verifyComplete();

        
        List<ChatRequest.Message> history = manager.getHistory(sessionId).orElseThrow();
        assertEquals(4, history.size());
    }

    @Test
    @DisplayName("会话不存在时报错")
    void testChatWithNonExistentSession() {
        StepVerifier.create(manager.chat("non-existent", "你好"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException 
                        && e.getMessage().contains("会话不存在"))
                .verify();
    }

    @Test
    @DisplayName("清空会话历史")
    void testClearSession() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setChoices(List.of(new Choice(0, new Message("assistant", "回复"), null)));
        when(bailianService.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        String sessionId = manager.createSession();
        manager.chat(sessionId, "测试").block();

        assertEquals(2, manager.getHistory(sessionId).orElseThrow().size());

        manager.clearSession(sessionId);
        assertTrue(manager.getHistory(sessionId).orElseThrow().isEmpty());
    }

    @Test
    @DisplayName("删除会话")
    void testRemoveSession() {
        String sessionId = manager.createSession();
        assertTrue(manager.getSession(sessionId).isPresent());

        manager.removeSession(sessionId);
        assertTrue(manager.getSession(sessionId).isEmpty());
    }

    @Test
    @DisplayName("设置系统提示词")
    void testSetSystemPrompt() {
        String sessionId = manager.createSession();
        manager.setSystemPrompt(sessionId, "新提示词");
        
        assertEquals("新提示词", manager.getSession(sessionId).orElseThrow().getSystemPrompt());
    }
}