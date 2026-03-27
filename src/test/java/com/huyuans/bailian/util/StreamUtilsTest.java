package com.huyuans.bailian.util;

import com.huyuans.bailian.model.response.ChatStreamResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StreamUtils.
 */
class StreamUtilsTest {

    private ChatStreamResponse createStreamResponse(String content, String finishReason) {
        ChatStreamResponse.Delta delta = new ChatStreamResponse.Delta();
        delta.setContent(content);

        ChatStreamResponse.Choice choice = new ChatStreamResponse.Choice();
        choice.setDelta(delta);
        choice.setFinishReason(finishReason);

        ChatStreamResponse response = new ChatStreamResponse();
        response.setChoices(Arrays.asList(choice));
        response.setId("test-id");
        response.setModel("test-model");
        return response;
    }

    @Test
    void testCollectText() {
        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("Hello", null),
                createStreamResponse(" World", "stop")
        );

        StepVerifier.create(StreamUtils.collectText(flux))
                .expectNext("Hello World")
                .verifyComplete();
    }

    @Test
    void testCollectTextEmpty() {
        StepVerifier.create(StreamUtils.collectText(Flux.empty()))
                .expectNext("")
                .verifyComplete();
    }

    @Test
    void testCollectResult() {
        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("Test", null),
                createStreamResponse(" Content", "stop")
        );

        StepVerifier.create(StreamUtils.collectResult(flux))
                .assertNext(r -> {
                    assertEquals("Test Content", r.getContent());
                    assertEquals("stop", r.getFinishReason());
                    assertTrue(r.isFinished());
                })
                .verifyComplete();
    }

    @Test
    void testStreamToWithCallback() {
        StringBuilder builder = new StringBuilder();
        List<String> callbackContents = new ArrayList<>();

        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("X", null),
                createStreamResponse("Y", null)
        );

        StepVerifier.create(StreamUtils.streamTo(flux, builder, callbackContents::add))
                .verifyComplete();

        assertEquals("XY", builder.toString());
        assertEquals(Arrays.asList("X", "Y"), callbackContents);
    }

    @Test
    void testStreamResultToString() {
        StreamUtils.StreamResult result = new StreamUtils.StreamResult("id1", "model1", "content", "stop");
        assertTrue(result.toString().contains("id1"));
        assertTrue(result.toString().contains("stop"));
    }
}
