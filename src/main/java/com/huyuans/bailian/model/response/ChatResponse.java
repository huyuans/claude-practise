package com.huyuans.bailian.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;























@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    


    private String id;

    


    private String object;

    


    private long created;

    


    private String model;

    




    private List<Choice> choices;

    


    private Usage usage;

    


    private Map<String, Object> error;

    




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        


        private int index;

        


        private Message message;

        






        private String finishReason;
    }

    


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        


        private String role;

        


        private String content;
    }

    


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        


        private int promptTokens;

        


        private int completionTokens;

        


        private int totalTokens;
    }
}