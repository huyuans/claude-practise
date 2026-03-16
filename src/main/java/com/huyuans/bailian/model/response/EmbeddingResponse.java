package com.huyuans.bailian.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedding 响应模型
 * <p>
 * 封装文本向量化结果，每个文本对应一个浮点数向量（通常为 1536 维）。
 * 向量可用于：
 * <ul>
 *   <li>语义搜索：通过向量相似度匹配相关文档</li>
 *   <li>聚类分析：将相似文本分组</li>
 *   <li>推荐系统：基于向量相似度推荐内容</li>
 * </ul>
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
     * 请求唯一标识
     */
    private String id;

    /**
     * 对象类型（通常为 "list"）
     */
    private String object;

    /**
     * 创建时间戳（Unix 时间）
     */
    private long created;

    /**
     * 使用的模型名称
     */
    private String model;

    /**
     * Embedding 结果列表
     * <p>
     * 与请求中的文本一一对应
     */
    private List<Embedding> embeddings;

    /**
     * Token 使用统计
     */
    private Usage usage;

    /**
     * Embedding 模型
     * <p>
     * 表示单个文本的向量结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Embedding {
        /**
         * 对象类型（通常为 "embedding"）
         */
        private String object;

        /**
         * Embedding 向量
         * <p>
         * 浮点数数组，维度取决于模型（text-embedding-v3 为 1536 维）
         */
        private List<Float> embedding;

        /**
         * 索引（对应请求中的文本位置）
         */
        private int index;
    }

    /**
     * Token 使用统计模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        /**
         * 输入 token 数
         */
        private int promptTokens;

        /**
         * 总 token 数
         */
        private int totalTokens;
    }
}