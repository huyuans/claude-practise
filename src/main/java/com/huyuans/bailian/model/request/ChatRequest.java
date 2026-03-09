package com.huyuans.bailian.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 聊天请求模型
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
     */
    private String model;

    /**
     * 消息列表
     */
    private List<Message> messages;

    /**
     * 生成温度（0-2），越高越随机
     */
    private Double temperature;

    /**
     * 核采样阈值（0-1）
     */
    private Double topP;

    /**
     * 生成token数量限制
     */
    private Integer maxTokens;

    /**
     * 是否流式输出
     */
    private Boolean stream = false;

    /**
     * 自定义参数
     */
    private Map<String, Object> extraParameters;

    /**
     * 消息模型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色：system, user, assistant
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
         * @return Message实例
         */
        public static Message user(String content) {
            return Message.builder().role("user").content(content).build();
        }

        /**
         * 创建系统消息
         *
         * @param content 消息内容
         * @return Message实例
         */
        public static Message system(String content) {
            return Message.builder().role("system").content(content).build();
        }

        /**
         * 创建助手消息
         *
         * @param content 消息内容
         * @return Message实例
         */
        public static Message assistant(String content) {
            return Message.builder().role("assistant").content(content).build();
        }
    }
}