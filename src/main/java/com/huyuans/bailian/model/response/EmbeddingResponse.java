package com.huyuans.bailian.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;















@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {

    


    private String id;

    


    private String object;

    


    private long created;

    


    private String model;

    




    private List<Embedding> embeddings;

    


    private Usage usage;

    




    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embedding {
        


        private String object;

        




        private List<Float> embedding;

        


        private int index;
    }

    


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        


        private int promptTokens;

        


        private int totalTokens;
    }
}