package com.huyuans.bailian.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 百炼模型配置属性类
 * <p>
 * 通过 application.yml/properties 配置，前缀为 "bailian"
 * 支持丰富的配置项，包括超时、重试、连接池、缓存、健康检查等。
 * <p>
 * 配置示例：
 * <pre>
 * bailian:
 *   api-key: ${DASHSCOPE_API_KEY}  # 推荐使用环境变量
 *   base-url: https://dashscope.aliyuncs.com
 *   default-model: qwen-turbo
 *   timeout: 60000
 *   retry:
 *     enabled: true
 *     max-attempts: 3
 *   connection-pool:
 *     enabled: true
 *     max-connections: 100
 *   embedding-cache:
 *     enabled: true
 *     max-size: 1000
 *     expire-minutes: 60
 *   conversation:
 *     enabled: true
 *     expire-minutes: 30
 *     max-tokens-per-session: 4000
 * </pre>
 *
 * @author Kasper
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "bailian")
public class BailianProperties {

    /**
     * API Key（支持环境变量 DASHSCOPE_API_KEY）
     * <p>
     * 从阿里云百炼控制台获取，建议使用环境变量避免硬编码
     */
    private String apiKey = "${DASHSCOPE_API_KEY:}";

    /**
     * API 基础 URL
     * <p>
     * 默认为阿里云百炼服务地址，私有化部署时需修改
     */
    private String baseUrl = "https://dashscope.aliyuncs.com";

    /**
     * 请求超时时间（毫秒）
     * <p>
     * 包括连接、读写在内的整体超时时间
     */
    private long timeout = 60000;

    /**
     * 默认聊天模型
     * <p>
     * 可选值：qwen-turbo, qwen-plus, qwen-max 等
     */
    private String defaultModel = "qwen-turbo";

    /**
     * 默认 Embedding 模型
     * <p>
     * 用于文本向量生成，可选值：text-embedding-v3 等
     */
    private String defaultEmbeddingModel = "text-embedding-v3";

    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * Embedding 缓存配置
     */
    private EmbeddingCacheConfig embeddingCache = new EmbeddingCacheConfig();

    /**
     * HTTP 连接池配置
     */
    private ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();

    /**
     * 健康检查配置
     */
    private HealthCheckConfig healthCheck = new HealthCheckConfig();

    /**
     * 指标配置
     */
    private MetricsConfig metrics = new MetricsConfig();

    /**
     * 会话管理配置
     */
    private Conversation conversation = new Conversation();

    /**
     * 重试配置
     */
    @Data
    public static class RetryConfig {
        /**
         * 是否启用自动重试
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
         * 延迟乘数（指数退避）
         */
        private double multiplier = 2.0;
    }

    /**
     * Embedding 缓存配置
     */
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

    /**
     * 连接池配置
     */
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

    /**
     * 健康检查配置
     */
    @Data
    public static class HealthCheckConfig {
        /**
         * 是否启用健康检查
         */
        private boolean enabled = false;

        /**
         * 健康检查间隔（毫秒）
         */
        private long interval = 60000;
    }

    /**
     * 指标配置
     */
    @Data
    public static class MetricsConfig {
        /**
         * 是否启用指标收集
         */
        private boolean enabled = true;
    }

    /**
     * 会话管理配置
     */
    @Data
    public static class Conversation {
        /**
         * 是否启用会话管理
         */
        private boolean enabled = false;

        /**
         * 会话过期时间（分钟）
         */
        private long expireMinutes = 60;

        /**
         * 单会话最大 token 数
         */
        private int maxTokensPerSession = 4000;
    }
}