package com.huyuans.bailian.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 流式聊天响应模型（SSE）
 * <p>
 * 用于处理 Server-Sent Events 流式响应，每次返回一小段文本增量。
 * 适合长文本生成场景，可以实时显示生成内容。
 * <p>
 * 流式响应特点：
 * <ul>
 *   <li>使用 SSE 协议，Content-Type: text/event-stream</li>
 *   <li>每次响应包含增量内容（delta），而非完整内容</li>
 *   <li>最后一个响应的 finishReason 标识结束</li>
 * </ul>
 *
 * @author Kasper
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamResponse {

    /**
     * 请求唯一标识
     */
    private String id;

    /**
     * 对象类型（通常为 "chat.completion.chunk"）
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
         * 流式增量消息
         * <p>
         * 仅包含本次新增的内容，需要累积拼接
         */
        private Delta delta;

        /**
         * 完成原因（最后一个响应才有值）
         */
        private String finishReason;
    }

    /**
     * 流式增量消息
     * <p>
     * 包含本次新增的角色或内容
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Delta {
        /**
         * 角色（通常在第一个响应中返回）
         */
        private String role;

        /**
         * 增量内容
         */
        private String content;
    }
}