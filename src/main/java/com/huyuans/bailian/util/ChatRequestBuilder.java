package com.huyuans.bailian.util;

import com.huyuans.bailian.model.request.ChatRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天请求构建器
 * <p>
 * 提供流畅的 API 来构建 ChatRequest，简化多轮对话的消息组装。
 * 使用链式调用，代码更清晰易读。
 * <p>
 * 使用示例：
 * <pre>
 * // 简单对话
 * ChatRequest request = ChatRequestBuilder.create()
 *     .user("你好")
 *     .build();
 * 
 * // 带系统提示的多轮对话
 * ChatRequest request = ChatRequestBuilder.create("qwen-max")
 *     .system("你是一个专业的程序员")
 *     .user("帮我写一个排序算法")
 *     .assistant("好的，这是一个快速排序...")
 *     .user("能优化一下吗？")
 *     .temperature(0.7)
 *     .maxTokens(2000)
 *     .build();
 * </pre>
 *
 * @author Kasper
 * @since 1.0.0
 */
public class ChatRequestBuilder {

    /** 消息列表 */
    private final List<ChatRequest.Message> messages = new ArrayList<>();
    
    /** 模型名称 */
    private String model;
    
    /** 温度参数 */
    private Double temperature;
    
    /** Top-P 参数 */
    private Double topP;
    
    /** 最大 token 数 */
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