package com.huyuans.bailian.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

































@Data
@ConfigurationProperties(prefix = "bailian")
public class BailianProperties {

    




    private String apiKey = "${DASHSCOPE_API_KEY:}";

    




    private String baseUrl = "https://dashscope.aliyuncs.com";

    




    private long timeout = 60000;

    




    private String defaultModel = "qwen-turbo";

    




    private String defaultEmbeddingModel = "text-embedding-v3";

    


    private RetryConfig retry = new RetryConfig();

    


    private EmbeddingCacheConfig embeddingCache = new EmbeddingCacheConfig();

    


    private ConnectionPoolConfig connectionPool = new ConnectionPoolConfig();

    


    private HealthCheckConfig healthCheck = new HealthCheckConfig();

    


    private MetricsConfig metrics = new MetricsConfig();

    


    private Conversation conversation = new Conversation();

    


    @Data
    public static class RetryConfig {
        


        private boolean enabled = true;

        


        private int maxAttempts = 3;

        


        private long initialDelay = 1000;

        


        private long maxDelay = 10000;

        


        private double multiplier = 2.0;
    }

    


    @Data
    public static class EmbeddingCacheConfig {
        


        private boolean enabled = false;

        


        private int maxSize = 1000;

        


        private long expireMinutes = 60;
    }

    


    @Data
    public static class ConnectionPoolConfig {
        


        private boolean enabled = true;

        


        private int maxConnections = 100;

        


        private int maxConnectionsPerHost = 50;

        


        private long idleTimeout = 20000;

        


        private long acquireTimeout = 10000;
    }

    


    @Data
    public static class HealthCheckConfig {
        


        private boolean enabled = false;

        


        private long interval = 60000;
    }

    


    @Data
    public static class MetricsConfig {
        


        private boolean enabled = true;
    }

    


    @Data
    public static class Conversation {
        


        private boolean enabled = false;

        


        private long expireMinutes = 60;

        


        private int maxTokensPerSession = 4000;
    }
}