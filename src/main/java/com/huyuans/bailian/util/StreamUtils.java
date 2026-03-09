package com.huyuans.bailian.util;

import com.huyuans.bailian.model.response.ChatStreamResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 流式响应工具类
 * <p>
 * 提供流式响应的处理和收集功能
 *
 * @author Kasper
 * @since 1.0.0
 */
public class StreamUtils {

    private StreamUtils() {}

    /**
     * 从流式响应中收集完整文本
     * <p>
     * 使用示例：
     * <pre>
     * String fullText = StreamUtils.collectText(flux).block();
     * </pre>
     *
     * @param flux 流式响应
     * @return 包含完整文本的 Mono
     */
    public static Mono<String> collectText(Flux<ChatStreamResponse> flux) {
        return flux
                .filter(response -> response.getChoices() != null && !response.getChoices().isEmpty())
                .map(response -> response.getChoices().get(0).getDelta())
                .filter(delta -> delta != null && delta.getContent() != null)
                .map(ChatStreamResponse.Delta::getContent)
                .collectList()
                .map(contents -> String.join("", contents));
    }

    /**
     * 从流式响应中收集完整文本，并统计 token 使用量
     * <p>
     * 返回一个包含完整文本和元信息的流式结果
     *
     * @param flux 流式响应
     * @return 包含完整文本和元信息的 Mono
     */
    public static Mono<StreamResult> collectResult(Flux<ChatStreamResponse> flux) {
        return flux
                .filter(response -> response.getChoices() != null && !response.getChoices().isEmpty())
                .collectList()
                .map(responses -> {
                    StringBuilder sb = new StringBuilder();
                    String finishReason = null;
                    String model = null;
                    String id = null;

                    for (ChatStreamResponse response : responses) {
                        if (model == null && response.getModel() != null) {
                            model = response.getModel();
                        }
                        if (id == null && response.getId() != null) {
                            id = response.getId();
                        }

                        ChatStreamResponse.Choice choice = response.getChoices().get(0);
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            sb.append(choice.getDelta().getContent());
                        }
                        if (choice.getFinishReason() != null) {
                            finishReason = choice.getFinishReason();
                        }
                    }

                    return new StreamResult(id, model, sb.toString(), finishReason);
                });
    }

    /**
     * 将流式响应转换为文本列表（按行分割）
     *
     * @param flux 流式响应
     * @return 文本片段列表
     */
    public static Mono<List<String>> collectLines(Flux<ChatStreamResponse> flux) {
        return flux
                .filter(response -> response.getChoices() != null && !response.getChoices().isEmpty())
                .map(response -> response.getChoices().get(0).getDelta())
                .filter(delta -> delta != null && delta.getContent() != null)
                .map(ChatStreamResponse.Delta::getContent)
                .collectList();
    }

    /**
     * 流式输出到 StringBuilder（适合实时显示场景）
     *
     * @param flux      流式响应
     * @param builder   用于收集文本的 StringBuilder
     * @param onContent 每次收到内容时的回调
     * @return 完成时的 Mono
     */
    public static Mono<Void> streamTo(Flux<ChatStreamResponse> flux, 
                                       StringBuilder builder, 
                                       Consumer<String> onContent) {
        return flux
                .filter(response -> response.getChoices() != null && !response.getChoices().isEmpty())
                .map(response -> response.getChoices().get(0).getDelta())
                .filter(delta -> delta != null && delta.getContent() != null)
                .map(ChatStreamResponse.Delta::getContent)
                .doOnNext(content -> {
                    builder.append(content);
                    if (onContent != null) {
                        onContent.accept(content);
                    }
                })
                .then();
    }

    /**
     * 流式输出到 StringBuilder（无回调）
     *
     * @param flux    流式响应
     * @param builder 用于收集文本的 StringBuilder
     * @return 完成时的 Mono
     */
    public static Mono<Void> streamTo(Flux<ChatStreamResponse> flux, StringBuilder builder) {
        return streamTo(flux, builder, null);
    }

    /**
     * 流式结果封装
     */
    public static class StreamResult {
        private final String id;
        private final String model;
        private final String content;
        private final String finishReason;

        public StreamResult(String id, String model, String content, String finishReason) {
            this.id = id;
            this.model = model;
            this.content = content;
            this.finishReason = finishReason;
        }

        public String getId() {
            return id;
        }

        public String getModel() {
            return model;
        }

        public String getContent() {
            return content;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public boolean isFinished() {
            return "stop".equals(finishReason);
        }

        public boolean isTruncated() {
            return "length".equals(finishReason);
        }

        @Override
        public String toString() {
            return "StreamResult{" +
                    "id='" + id + '\'' +
                    ", model='" + model + '\'' +
                    ", contentLength=" + (content != null ? content.length() : 0) +
                    ", finishReason='" + finishReason + '\'' +
                    '}';
        }
    }
}