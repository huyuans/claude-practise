package com.huyuans.bailian.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 聊天响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

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
     * 使用统计
     */
    private Usage usage;

    /**
     * 错误信息（如果有）
     */
    private Map<String, Object> error;

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
         * 消息
         */
        private Message message;

        /**
         * 完成原因（stop, length, content_filter）
         */
        private String finishReason;
    }

    /**
     * 消息模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;
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
         * 输出token数
         */
        private int completionTokens;

        /**
         * 总token数
         */
        private int totalTokens;
    }
}