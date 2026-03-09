package com.huyuans.bailian.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedding响应模型
 *
 * @author Kasper
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {

    /**
     * 模型ID
     */
    private String id;

    /**
     * 对象类型
     */
    private String object;

    /**
     * 创建时间戳
     */
    private long created;

    /**
     * 模型名称
     */
    private String model;

    /**
     * Embedding结果列表
     */
    private List<Embedding> embeddings;

    /**
     * 使用统计
     */
    private Usage usage;

    /**
     * Embedding模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embedding {
        /**
         * 对象类型
         */
        private String object;

        /**
         * Embedding向量
         */
        private List<Float> embedding;

        /**
         * 索引
         */
        private int index;
    }

    /**
     * 使用统计模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 输入token数
         */
        private int promptTokens;

        /**
         * 总token数
         */
        private int totalTokens;
    }
}