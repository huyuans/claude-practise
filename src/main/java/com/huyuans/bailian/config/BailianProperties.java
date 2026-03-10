package com.huyuans.bailian.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 百炼模型配置属性类
 *
 * @author Kasper
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "bailian")
public class BailianProperties {

    /**
     * API Key（支持环境变量 DASHSCOPE_API_KEY）
     */
    private String apiKey = "${DASHSCOPE_API_KEY:}";

    /**
     * 基础URL
     */
    private String baseUrl = "https://dashscope.aliyuncs.com";

    /**
     * 请求超时时间（毫秒）
     */
    private long timeout = 60000;

    /**
     * 默认聊天模型
     */
    private String defaultModel = "qwen-turbo";

    /**
     * 默认Embedding模型
     */
    private String defaultEmbeddingModel = "text-embedding-v3";

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * Embedding缓存配置
     */
    private EmbeddingCacheConfig embeddingCache = new EmbeddingCacheConfig();

    /**
     * 连接池配置
     */
    private ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();

    @Data
    public static class RetryConfig {
        /**
         * 是否启用重试
         */
        private boolean enabled = true;

        /**
         * 最大重试次数
         */
        private int maxAttempts = 3;

        /**
         * 初始延迟（毫秒）
         */
        private long initialDelay = 1000;

        /**
         * 最大延迟（毫秒）
         */
        private long maxDelay = 10000;

        /**
         * 延迟乘数
         */
        private double multiplier = 2.0;
    }

    @Data
    public static class EmbeddingCacheConfig {
        /**
         * 是否启用缓存
         */
        private boolean enabled = false;

        /**
         * 缓存最大条目数
         */
        private int maxSize = 1000;

        /**
         * 缓存过期时间（分钟）
         */
        private long expireMinutes = 60;
    }

    @Data
    public static class ConnectionPoolConfig {
        /**
         * 是否启用连接池
         */
        private boolean enabled = true;

        /**
         * 最大连接数
         */
        private int maxConnections = 100;

        /**
         * 每个主机的最大连接数
         */
        private int maxConnectionsPerHost = 50;

        /**
         * 连接空闲超时时间（毫秒）
         */
        private long idleTimeout = 20000;

        /**
         * 连接获取超时时间（毫秒）
         */
        private long acquireTimeout = 10000;
    }

    }