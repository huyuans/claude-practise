package com.huyuans.bailian.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 聊天请求模型
 * <p>
 * 封装百炼聊天 API 的请求参数，支持多轮对话、温度控制、token 限制等功能。
 * 使用 Builder 模式构建，提供流畅的 API。
 * <p>
 * 使用示例：
 * <pre>
 * ChatRequest request = ChatRequest.builder()
 *     .model("qwen-turbo")
 *     .messages(List.of(
 *         ChatRequest.Message.system("你是一个助手"),
 *         ChatRequest.Message.user("你好")
 *     ))
 *     .temperature(0.7)
 *     .maxTokens(1000)
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
public class ChatRequest {

    /**
     * 模型名称
     * <p>
     * 可选值：qwen-turbo（快速）、qwen-plus（均衡）、qwen-max（效果最好）
     */
    private String model;

    /**
     * 消息列表
     * <p>
     * 支持多轮对话，按顺序包含 system、user、assistant 消息
     */
    private List<Message> messages;

    /**
     * 生成温度（0-2）
     * <p>
     * 越高越随机，越低越确定。推荐值：0.7
     */
    private Double temperature;

    /**
     * 核采样阈值（0-1）
     * <p>
     * 控制生成多样性，与 temperature 二选一。推荐值：0.8
     */
    private Double topP;

    /**
     * 生成 token 数量限制
     * <p>
     * 最大输出长度，超时会截断
     */
    private Integer maxTokens;

    /**
     * 是否流式输出
     * <p>
     * true 时使用 SSE 流式返回，适合长文本生成
     */
    private Boolean stream = false;

    /**
     * 自定义参数
     * <p>
     * 用于传递模型特定的扩展参数
     */
    private Map<String, Object> extraParameters;

    /**
     * 消息模型
     * <p>
     * 表示对话中的一条消息，包含角色和内容
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色
         * <p>
         * 可选值：system（系统提示）、user（用户消息）、assistant（助手回复）
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;

        /**
         * 创建用户消息
         *
         * @param content 消息内容
         * @return Message 实例
         */
        public static Message user(String content) {
            return Message.builder().role("user").content(content).build();
        }

        /**
         * 创建系统消息
         *
         * @param content 消息内容
         * @return Message 实例
         */
        public static Message system(String content) {
            return Message.builder().role("system").content(content).build();
        }

        /**
         * 创建助手消息
         *
         * @param content 消息内容
         * @return Message 实例
         */
        public static Message assistant(String content) {
            return Message.builder().role("assistant").content(content).build();
        }
    }
}