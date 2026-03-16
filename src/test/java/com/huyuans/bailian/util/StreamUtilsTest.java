package com.huyuans.bailian.util;

import com.huyuans.bailian.model.response.ChatStreamResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StreamUtils 单元测试
 * <p>
 * 测试流式响应工具类的功能，包括：
 * <ul>
 *   <li>流式响应文本收集</li>
 *   <li>流式结果元信息提取</li>
 *   <li>StringBuilder 实时输出</li>
 *   <li>回调函数触发</li>
 * </ul>
 *
 * @author Kasper
 * @since 1.0.0
 */
@DisplayName("流式响应工具类测试")
class StreamUtilsTest {

    private ChatStreamResponse createStreamResponse(String content, String finishReason) {
        ChatStreamResponse.Delta delta = new ChatStreamResponse.Delta();
        delta.setContent(content);

        ChatStreamResponse.Choice choice = new ChatStreamResponse.Choice();
        choice.setDelta(delta);
        choice.setFinishReason(finishReason);

        ChatStreamResponse response = new ChatStreamResponse();
        response.setChoices(List.of(choice));
        response.setId("test-id");
        response.setModel("test-model");

        return response;
    }

    private ChatStreamResponse createEmptyResponse() {
        ChatStreamResponse response = new ChatStreamResponse();
        response.setChoices(null);
        return response;
    }

    @Test
    @DisplayName("收集流式响应文本")
    void testCollectText() {
        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("Hello", null),
                createStreamResponse(" World", null),
                createStreamResponse("!", "stop")
        );

        StepVerifier.create(StreamUtils.collectText(flux))
                .expectNext("Hello World!")
                .verifyComplete();
    }

    @Test
    @DisplayName("空流返回空字符串")
    void testCollectTextEmpty() {
        Flux<ChatStreamResponse> flux = Flux.empty();

        StepVerifier.create(StreamUtils.collectText(flux))
                .expectNext("")
                .verifyComplete();
    }

    @Test
    @DisplayName("过滤空选择列表")
    void testCollectTextFilterNullChoices() {
        Flux<ChatStreamResponse> flux = Flux.just(
                createEmptyResponse(),
                createStreamResponse("Valid", null)
        );

        StepVerifier.create(StreamUtils.collectText(flux))
                .expectNext("Valid")
                .verifyComplete();
    }

    @Test
    @DisplayName("收集流式结果")
    void testCollectResult() {
        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("Test", null),
                createStreamResponse(" Content", "stop")
        );

        StepVerifier.create(StreamUtils.collectResult(flux))
                .assertNext(result -> {
                    assertEquals("Test Content", result.getContent());
                    assertEquals("stop", result.getFinishReason());
                    assertEquals("test-id", result.getId());
                    assertEquals("test-model", result.getModel());
                    assertTrue(result.isFinished());
                    assertFalse(result.isTruncated());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("流式结果截断状态")
    void testStreamResultTruncated() {
        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("Partial", "length")
        );

        StepVerifier.create(StreamUtils.collectResult(flux))
                .assertNext(result -> {
                    assertTrue(result.isTruncated());
                    assertFalse(result.isFinished());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("收集文本行列表")
    void testCollectLines() {
        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("Line1", null),
                createStreamResponse("Line2", null)
        );

        StepVerifier.create(StreamUtils.collectLines(flux))
                .expectNext(List.of("Line1", "Line2"))
                .verifyComplete();
    }

    @Test
    @DisplayName("流式输出到StringBuilder")
    void testStreamToStringBuilder() {
        StringBuilder builder = new StringBuilder();
        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("A", null),
                createStreamResponse("B", null)
        );

        StepVerifier.create(StreamUtils.streamTo(flux, builder))
                .verifyComplete();

        assertEquals("AB", builder.toString());
    }

    @Test
    @DisplayName("流式输出带回调")
    void testStreamWithCallback() {
        StringBuilder builder = new StringBuilder();
        List<String> callbackContents = new ArrayList<>();
        Flux<ChatStreamResponse> flux = Flux.just(
                createStreamResponse("X", null),
                createStreamResponse("Y", null)
        );

        StepVerifier.create(StreamUtils.streamTo(flux, builder, callbackContents::add))
                .verifyComplete();

        assertEquals("XY", builder.toString());
        assertEquals(List.of("X", "Y"), callbackContents);
    }

    @Test
    @DisplayName("StreamResult toString")
    void testStreamResultToString() {
        StreamUtils.StreamResult result = new StreamUtils.StreamResult("id1", "model1", "content", "stop");
        String str = result.toString();
        assertTrue(str.contains("id1"));
        assertTrue(str.contains("model1"));
        assertTrue(str.contains("contentLength=7"));
        assertTrue(str.contains("stop"));
    }

    @Test
    @DisplayName("空delta内容过滤")
    void testFilterNullDeltaContent() {
        ChatStreamResponse responseWithNullDelta = new ChatStreamResponse();
        ChatStreamResponse.Choice choice = new ChatStreamResponse.Choice();
        ChatStreamResponse.Delta nullDelta = new ChatStreamResponse.Delta();
        nullDelta.setContent(null);
        choice.setDelta(nullDelta);
        responseWithNullDelta.setChoices(List.of(choice));

        Flux<ChatStreamResponse> flux = Flux.just(
                responseWithNullDelta,
                createStreamResponse("Valid", null)
        );

        StepVerifier.create(StreamUtils.collectText(flux))
                .expectNext("Valid")
                .verifyComplete();
    }
}