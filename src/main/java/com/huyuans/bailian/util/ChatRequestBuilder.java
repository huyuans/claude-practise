package com.huyuans.bailian.util;

import com.huyuans.bailian.model.request.ChatRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天请求构建器（提供更流畅的API）
 * <p>
 * 使用示例：
 * <pre>
 * ChatRequest request = ChatRequestBuilder.create()
 *     .system("你是一个有帮助的助手")
 *     .user("你好")
 *     .assistant("你好！有什么可以帮助你的？")
 *     .user("介绍一下你自己")
 *     .temperature(0.8)
 *     .build();
 * </pre>
 *
 * @author Kasper
 * @since 1.0.0
 */
public class ChatRequestBuilder {

    private final List<ChatRequest.Message> messages = new ArrayList<>();
    private String model;
    private Double temperature;
    private Double topP;
    private Integer maxTokens;

    private ChatRequestBuilder() {}

    /**
     * 创建构建器
     */
    public static ChatRequestBuilder create() {
        return new ChatRequestBuilder();
    }

    /**
     * 创建构建器并指定模型
     */
    public static ChatRequestBuilder create(String model) {
        return new ChatRequestBuilder().model(model);
    }

    /**
     * 设置模型
     */
    public ChatRequestBuilder model(String model) {
        this.model = model;
        return this;
    }

    /**
     * 设置温度
     */
    public ChatRequestBuilder temperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * 设置top_p
     */
    public ChatRequestBuilder topP(Double topP) {
        this.topP = topP;
        return this;
    }

    /**
     * 设置最大token数
     */
    public ChatRequestBuilder maxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    /**
     * 添加系统消息
     */
    public ChatRequestBuilder system(String content) {
        messages.add(ChatRequest.Message.system(content));
        return this;
    }

    /**
     * 添加用户消息
     */
    public ChatRequestBuilder user(String content) {
        messages.add(ChatRequest.Message.user(content));
        return this;
    }

    /**
     * 添加助手消息
     */
    public ChatRequestBuilder assistant(String content) {
        messages.add(ChatRequest.Message.assistant(content));
        return this;
    }

    /**
     * 添加消息
     */
    public ChatRequestBuilder message(String role, String content) {
        messages.add(ChatRequest.Message.builder().role(role).content(content).build());
        return this;
    }

    /**
     * 构建请求
     */
    public ChatRequest build() {
        return ChatRequest.builder()
                .model(model)
                .messages(new ArrayList<>(messages))
                .temperature(temperature)
                .topP(topP)
                .maxTokens(maxTokens)
                .build();
    }
}