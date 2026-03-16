package com.huyuans.bailian.util;

import com.huyuans.bailian.model.response.ChatStreamResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;






























public class StreamUtils {

    
    private StreamUtils() {}

    










    public static Mono<String> collectText(Flux<ChatStreamResponse> flux) {
        return flux
                .filter(response -> response.getChoices() != null && !response.getChoices().isEmpty())
                .map(response -> response.getChoices().get(0).getDelta())
                .filter(delta -> delta != null && delta.getContent() != null)
                .map(ChatStreamResponse.Delta::getContent)
                .collectList()
                .map(contents -> String.join("", contents));
    }

    







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

    





    public static Mono<List<String>> collectLines(Flux<ChatStreamResponse> flux) {
        return flux
                .filter(response -> response.getChoices() != null && !response.getChoices().isEmpty())
                .map(response -> response.getChoices().get(0).getDelta())
                .filter(delta -> delta != null && delta.getContent() != null)
                .map(ChatStreamResponse.Delta::getContent)
                .collectList();
    }

    







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

    






    public static Mono<Void> streamTo(Flux<ChatStreamResponse> flux, StringBuilder builder) {
        return streamTo(flux, builder, null);
    }

    


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