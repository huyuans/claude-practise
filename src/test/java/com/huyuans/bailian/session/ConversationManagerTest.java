package com.huyuans.bailian.session;

import com.huyuans.bailian.model.request.ChatRequest;
import com.huyuans.bailian.model.response.ChatResponse;
import com.huyuans.bailian.model.response.ChatResponse.Choice;
import com.huyuans.bailian.model.response.ChatResponse.Message;
import com.huyuans.bailian.service.BailianService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConversationManager.
 */
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
    void testCreateSession() {
        String sessionId = manager.createSession();
        assertNotNull(sessionId);
        assertTrue(manager.getSession(sessionId).isPresent());
        assertEquals(1, manager.getActiveSessionCount());
    }

    @Test
    void testCreateSessionWithSystemPrompt() {
        String sessionId = manager.createSession("You are an assistant");
        ConversationSession session = manager.getSession(sessionId).orElseThrow();
        assertEquals("You are an assistant", session.getSystemPrompt());
    }

    @Test
    void testChat() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setChoices(Arrays.asList(new Choice(0, new Message("assistant", "Hello!"), null)));
        when(bailianService.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        String sessionId = manager.createSession();

        StepVerifier.create(manager.chat(sessionId, "Hello"))
                .expectNext("Hello!")
                .verifyComplete();

        List<ChatRequest.Message> history = manager.getHistory(sessionId).orElseThrow();
        assertEquals(2, history.size());
        assertEquals("user", history.get(0).getRole());
        assertEquals("assistant", history.get(1).getRole());
    }

    @Test
    void testMultiTurnChat() {
        ChatResponse response1 = new ChatResponse();
        response1.setChoices(Arrays.asList(new Choice(0, new Message("assistant", "Reply 1"), null)));

        ChatResponse response2 = new ChatResponse();
        response2.setChoices(Arrays.asList(new Choice(0, new Message("assistant", "Reply 2"), null)));

        when(bailianService.chat(any(ChatRequest.class)))
                .thenReturn(Mono.just(response1))
                .thenReturn(Mono.just(response2));

        String sessionId = manager.createSession();

        StepVerifier.create(manager.chat(sessionId, "Q1")).expectNext("Reply 1").verifyComplete();
        StepVerifier.create(manager.chat(sessionId, "Q2")).expectNext("Reply 2").verifyComplete();

        assertEquals(4, manager.getHistory(sessionId).orElseThrow().size());
    }

    @Test
    void testChatWithNonExistentSession() {
        StepVerifier.create(manager.chat("non-existent", "Hello"))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("non-existent"))
                .verify();
    }

    @Test
    void testClearSession() {
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setChoices(Arrays.asList(new Choice(0, new Message("assistant", "Reply"), null)));
        when(bailianService.chat(any(ChatRequest.class))).thenReturn(Mono.just(mockResponse));

        String sessionId = manager.createSession();
        manager.chat(sessionId, "Test").block();

        assertEquals(2, manager.getHistory(sessionId).orElseThrow().size());

        manager.clearSession(sessionId);
        assertTrue(manager.getHistory(sessionId).orElseThrow().isEmpty());
    }

    @Test
    void testRemoveSession() {
        String sessionId = manager.createSession();
        assertTrue(manager.getSession(sessionId).isPresent());

        manager.removeSession(sessionId);
        assertTrue(manager.getSession(sessionId).isEmpty());
    }

    @Test
    void testSetSystemPrompt() {
        String sessionId = manager.createSession();
        manager.setSystemPrompt(sessionId, "New prompt");

        assertEquals("New prompt", manager.getSession(sessionId).orElseThrow().getSystemPrompt());
    }
}
