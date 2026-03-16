package com.huyuans.bailian.client;

import lombok.Getter;

/**
 * 百炼模型调用异常
 * <p>
 * 封装百炼 API 调用过程中发生的各类异常，提供统一的错误处理接口。
 * 包含 HTTP 状态码和原始响应体，便于问题定位和错误重试判断。
 * <p>
 * 常见错误场景：
 * <ul>
 *   <li>HTTP 401 - API Key 无效或过期</li>
 *   <li>HTTP 429 - 请求限流，需要重试</li>
 *   <li>HTTP 500/502/503 - 服务端错误</li>
 *   <li>网络异常 - 连接超时或中断</li>
 * </ul>
 *
 * @author Kasper
 * @since 1.0.0
 */
@Getter
public class BailianException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** HTTP状态码（网络异常时为 0） */
    private final int httpStatus;

    /** 原始响应体（用于错误详情分析） */
    private final String responseBody;

    /**
     * 构造方法
     *
     * @param message 异常消息
     */
    public BailianException(String message) {
        super(message);
        this.httpStatus = 0;
        this.responseBody = null;
    }

    /**
     * 构造方法
     *
     * @param message      异常消息
     * @param httpStatus   HTTP状态码
     * @param responseBody 响应体
     */
    public BailianException(String message, int httpStatus, String responseBody) {
        super(message);
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    /**
     * 构造方法
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public BailianException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 0;
        this.responseBody = null;
    }
}