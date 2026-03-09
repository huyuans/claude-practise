package com.huyuans.bailian.autoconfigure;

import com.huyuans.bailian.cache.EmbeddingCache;
import com.huyuans.bailian.client.BailianClient;
import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.metrics.BailianMetricsRecorder;
import com.huyuans.bailian.service.BailianService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 百炼模型自动配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(BailianProperties.class)
@ConditionalOnProperty(prefix = "bailian", name = "api-key")
public class BailianAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BailianClient bailianClient(BailianProperties properties) {
        return new BailianClient(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingCache embeddingCache(BailianProperties properties) {
        return new EmbeddingCache(properties.getEmbeddingCache());
    }

    @Bean
    @ConditionalOnMissingBean
    public BailianMetricsRecorder bailianMetricsRecorder(
            @Autowired(required = false) MeterRegistry meterRegistry) {
        return new BailianMetricsRecorder(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public BailianService bailianService(BailianClient bailianClient, 
                                          BailianProperties properties,
                                          EmbeddingCache embeddingCache,
                                          BailianMetricsRecorder metricsRecorder) {
        return new BailianService(bailianClient, properties, embeddingCache, metricsRecorder);
    }
}