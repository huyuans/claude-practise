package com.huyuans.bailian.client;

import com.huyuans.bailian.config.BailianProperties;
import com.huyuans.bailian.model.request.ChatRequest;
import com.huyuans.bailian.model.request.EmbeddingRequest;
import com.huyuans.bailian.model.response.ChatResponse;
import com.huyuans.bailian.model.response.ChatStreamResponse;
import com.huyuans.bailian.model.response.EmbeddingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;




















@Slf4j
@Component
public class BailianClient {

    private final WebClient webClient;
    private final BailianProperties properties;

    public BailianClient(BailianProperties properties) {
        this.properties = properties;

        final HttpClient httpClient = createHttpClient(properties);

        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    




    private HttpClient createHttpClient(BailianProperties props) {
        HttpClient httpClient = HttpClient.create();

        
        if (props.getConnectionPool().isEnabled()) {
            BailianProperties.ConnectionPoolConfig poolConfig = props.getConnectionPool();
            ConnectionProvider provider = ConnectionProvider.builder("bailian-pool")
                    .maxConnections(poolConfig.getMaxConnections())           
                    .pendingAcquireTimeout(Duration.ofMillis(poolConfig.getAcquireTimeout()))  
                    .maxIdleTime(Duration.ofMillis(poolConfig.getIdleTimeout()))  
                    .build();

            httpClient = HttpClient.create(provider);
            log.info("百炼API连接池已启用: maxConnections={}", poolConfig.getMaxConnections());
        }

        
        return httpClient.responseTimeout(Duration.ofMillis(props.getTimeout()));
    }

    





    public Mono<ChatResponse> chat(ChatRequest request) {
        String correlationId = generateCorrelationId();
        log.debug("[{}] 发送聊天请求, model={}", correlationId, request.getModel());

        return webClient.post()
                .uri("/api/v1/services/aigc/text-generation/generation")
                .header("X-Correlation-ID", correlationId)
                .bodyValue(buildChatRequest(request))
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .doOnSuccess(response -> log.debug("[{}] 聊天请求成功, model={}", correlationId, request.getModel()))
                .transform(this::applyRetry)
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("[{}] 调用百炼聊天API失败: HTTP={}, Body={}", correlationId, e.getStatusCode().value(), e.getResponseBodyAsString());
                    return Mono.error(new BailianException("调用百炼聊天API失败: " + e.getStatusText(),
                            e.getStatusCode().value(), e.getResponseBodyAsString()));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("[{}] 调用百炼聊天API异常: {}", correlationId, e.getMessage());
                    return Mono.error(new BailianException("调用百炼聊天API异常: " + e.getMessage(), e));
                });
    }

    





    public Flux<ChatStreamResponse> chatStream(ChatRequest request) {
        String correlationId = generateCorrelationId();
        log.debug("[{}] 发送流式聊天请求, model={}", correlationId, request.getModel());

        return webClient.post()
                .uri("/api/v1/services/aigc/text-generation/generation")
                .header("X-Correlation-ID", correlationId)
                .bodyValue(buildStreamChatRequest(request))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(ChatStreamResponse.class)
                .doOnComplete(() -> log.debug("[{}] 流式聊天完成", correlationId))
                .doOnError(WebClientResponseException.class, e -> {
                    log.error("[{}] 调用百炼流式聊天API失败: HTTP={}, Body={}", correlationId, e.getStatusCode().value(), e.getResponseBodyAsString());
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("[{}] 调用百炼流式聊天API异常: {}", correlationId, e.getMessage());
                    return Flux.error(new BailianException("调用百炼流式聊天API异常: " + e.getMessage(), e));
                });
    }

    





    public Mono<EmbeddingResponse> embedding(EmbeddingRequest request) {
        String correlationId = generateCorrelationId();
        log.debug("[{}] 发送Embedding请求, model={}", correlationId, request.getModel());

        return webClient.post()
                .uri("/api/v1/services/aigc/text-embedding/embedding")
                .header("X-Correlation-ID", correlationId)
                .bodyValue(buildEmbeddingRequest(request))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .doOnSuccess(response -> log.debug("[{}] Embedding请求成功", correlationId))
                .transform(this::applyRetry)
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("[{}] 调用百炼Embedding API失败: HTTP={}, Body={}", correlationId, e.getStatusCode().value(), e.getResponseBodyAsString());
                    return Mono.error(new BailianException("调用百炼Embedding API失败: " + e.getStatusText(),
                            e.getStatusCode().value(), e.getResponseBodyAsString()));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("[{}] 调用百炼Embedding API异常: {}", correlationId, e.getMessage());
                    return Mono.error(new BailianException("调用百炼Embedding API异常: " + e.getMessage(), e));
                });
    }

    


    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    





    private <T> Mono<T> applyRetry(Mono<T> source) {
        if (!properties.getRetry().isEnabled()) {
            return source;
        }

        BailianProperties.RetryConfig retryConfig = properties.getRetry();
        return source.retryWhen(Retry.backoff(retryConfig.getMaxAttempts(), Duration.ofMillis(retryConfig.getInitialDelay()))
                .maxBackoff(Duration.ofMillis(retryConfig.getMaxDelay()))  
                .filter(this::isRetryable)  
                .doBeforeRetry(signal -> log.warn("重试请求, 第{}次, 原因: {}", 
                        signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    


    private boolean isRetryable(Throwable error) {
        if (error instanceof WebClientResponseException e) {
            int status = e.getStatusCode().value();
            
            return status >= 500 || status == 429;
        }
        
        return true;
    }

    





    private Map<String, Object> buildChatRequest(ChatRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("messages", request.getMessages());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);
        parameters.put("top_p", request.getTopP() != null ? request.getTopP() : 0.8);
        parameters.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 1000);
        parameters.put("stream", false);

        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("input", input);
        body.put("parameters", parameters);
        return body;
    }

    





    private Map<String, Object> buildStreamChatRequest(ChatRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("messages", request.getMessages());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);
        parameters.put("top_p", request.getTopP() != null ? request.getTopP() : 0.8);
        parameters.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 1000);
        parameters.put("stream", true);

        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("input", input);
        body.put("parameters", parameters);
        return body;
    }

    





    private Map<String, Object> buildEmbeddingRequest(EmbeddingRequest request) {
        Map<String, Object> input = new HashMap<>();
        input.put("texts", request.getInput());

        Map<String, Object> body = new HashMap<>();
        body.put("model", request.getModel());
        body.put("input", input);
        return body;
    }
}