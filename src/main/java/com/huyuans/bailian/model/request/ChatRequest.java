package com.huyuans.bailian.model.request;

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
public class ChatRequest {

    




    private String model;

    




    private List<Message> messages;

    




    private Double temperature;

    




    private Double topP;

    




    private Integer maxTokens;

    




    private Boolean stream = false;

    




    private Map<String, Object> extraParameters;

    




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        




        private String role;

        


        private String content;

        





        public static Message user(String content) {
            return Message.builder().role("user").content(content).build();
        }

        





        public static Message system(String content) {
            return Message.builder().role("system").content(content).build();
        }

        





        public static Message assistant(String content) {
            return Message.builder().role("assistant").content(content).build();
        }
    }
}