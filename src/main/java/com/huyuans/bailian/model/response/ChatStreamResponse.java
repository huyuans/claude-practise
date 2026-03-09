package com.huyuans.bailian.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 流式聊天响应模型（SSE）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamResponse {

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
     * 输出内容列表
     */
    private List<Choice> choices;

    /**
     * 选择项模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        /**
         * 索引
         */
        private int index;

        /**
         * 流式消息
         */
        private Delta delta;

        /**
         * 完成原因
         */
        private String finishReason;
    }

    /**
     * 流式增量消息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Delta {
        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;
    }
}