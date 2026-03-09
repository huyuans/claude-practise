package com.huyuans.bailian.client;

import lombok.Getter;

/**
 * 百炼模型调用异常
 *
 * @author Kasper
 * @since 1.0.0
 */
@Getter
public class BailianException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** HTTP状态码 */
    private final int httpStatus;

    /** 响应体 */
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