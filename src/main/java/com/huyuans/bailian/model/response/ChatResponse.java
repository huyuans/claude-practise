package com.huyuans.bailian.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 聊天响应模型
 * <p>
 * 封装百炼聊天 API 的响应数据，包含生成的文本内容、token 使用统计等信息。
 * <p>
 * 响应结构示例：
 * <pre>
 * {
 *   "id": "chatcmpl-xxx",
 *   "model": "qwen-turbo",
 *   "choices": [{
 *     "index": 0,
 *     "message": {"role": "assistant", "content": "你好！"},
 *     "finishReason": "stop"
 *   }],
 *   "usage": {"promptTokens": 10, "completionTokens": 5, "totalTokens": 15}
 * }
 * </pre>
 *
 * @author Kasper
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 请求唯一标识
     */
    private String id;

    /**
     * 对象类型（通常为 "chat.completion"）
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
     * <p>
     * 通常只包含一个元素，除非设置了 n 参数
     */
    private List<Choice> choices;

    /**
     * Token 使用统计
     */
    private Usage usage;

    /**
     * 错误信息（请求失败时返回）
     */
    private Map<String, Object> error;

    /**
     * 选择项模型
     * <p>
     * 表示一个完整的生成结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        /**
         * 索引（当 n > 1 时用于区分）
         */
        private int index;

        /**
         * 生成的消息
         */
        private Message message;

        /**
         * 完成原因
         * <p>
         * - stop：正常完成
         * - length：达到 max_tokens 限制
         * - content_filter：内容过滤触发
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
         * 角色（通常为 "assistant"）
         */
        private String role;

        /**
         * 生成的文本内容
         */
        private String content;
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
         * 输出 token 数
         */
        private int completionTokens;

        /**
         * 总 token 数
         */
        private int totalTokens;
    }
}