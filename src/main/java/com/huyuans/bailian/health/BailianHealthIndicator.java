package com.huyuans.bailian.health;

import com.huyuans.bailian.config.BailianProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * 百炼 API 健康检查指示器
 * <p>
 * 实现 Spring Boot Actuator 的 HealthIndicator 接口，通过调用百炼模型列表接口检查服务可用性。
 * 适用于 Kubernetes 探针、负载均衡健康检查等场景。
 * <p>
 * 启用方式：配置 bailian.health-check.enabled=true
 * <p>
 * 健康检查端点：/actuator/health
 *
 * @author Kasper
 * @since 1.0.0
 * @see HealthIndicator Spring Boot 健康检查接口
 */
@Slf4j
@Component
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnProperty(prefix = "bailian.health-check", name = "enabled", havingValue = "true", matchIfMissing = false)
public class BailianHealthIndicator implements HealthIndicator {

    /** 健康检查 API 路径 */
    private static final String HEALTH_CHECK_PATH = "/api/v1/models";
    
    /** 健康检查超时时间（毫秒） */
    private static final long HEALTH_CHECK_TIMEOUT_MS = 5000;

    /** WebClient 实例 */
    private final WebClient webClient;
    
    /** 配置属性 */
    private final BailianProperties properties;

    public BailianHealthIndicator(BailianProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .build();
    }

    @Override
    public Health health() {
        log.debug("开始百炼 API 健康检查, baseUrl: {}", properties.getBaseUrl());
        try {
            // 简单检查：发送请求检查 API 是否可达
            log.info("正在检查百炼 API 可用性...");
            String response = webClient.get()
                    .uri(HEALTH_CHECK_PATH)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(HEALTH_CHECK_TIMEOUT_MS))
                    .block();

            if (response != null && !response.isEmpty()) {
                return Health.up()
                        .withDetail("baseUrl", properties.getBaseUrl())
                        .withDetail("defaultModel", properties.getDefaultModel())
                        .withDetail("status", "API 响应正常")
                        .build();
            }

            return Health.down()
                    .withDetail("error", "API 返回空响应")
                    .build();

        } catch (WebClientResponseException e) {
            log.warn("百炼 API 健康检查失败: HTTP {}", e.getStatusCode().value());
            return Health.down()
                    .withDetail("error", "HTTP " + e.getStatusCode().value())
                    .withDetail("message", e.getStatusText())
                    .build();
        } catch (Exception e) {
            log.warn("百炼 API 健康检查失败: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getClass().getSimpleName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}