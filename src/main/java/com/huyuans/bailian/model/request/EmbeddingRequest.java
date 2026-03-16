package com.huyuans.bailian.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedding 请求模型
 * <p>
 * 用于将文本转换为向量表示，支持批量处理多条文本。
 * 生成的向量可用于语义搜索、聚类、相似度计算等场景。
 * <p>
 * 使用示例：
 * <pre>
 * EmbeddingRequest request = EmbeddingRequest.builder()
 *     .model("text-embedding-v3")
 *     .input(List.of("你好", "世界"))
 *     .build();
 * </pre>
 *
 * @author Kasper
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {

    /**
     * 模型名称
     * <p>
     * 推荐：text-embedding-v3（最新版本）
     */
    private String model;

    /**
     * 输入文本（支持批量）
     * <p>
     * 一次请求最多处理 25 条文本，单条最长 2048 tokens
     */
    private List<String> input;
}