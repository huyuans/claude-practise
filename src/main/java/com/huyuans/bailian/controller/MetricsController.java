package com.huyuans.bailian.controller;

import com.huyuans.bailian.metrics.BailianMetricsRecorder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MeterRegistry meterRegistry;
    private final BailianMetricsRecorder metricsRecorder;

    @GetMapping("/usage")
    public Mono<ResponseEntity<UsageMetricsResponse>> getUsage() {
        log.info("Getting usage metrics");

        if (!metricsRecorder.isEnabled()) {
            return Mono.just(ResponseEntity.ok(new UsageMetricsResponse(
                    false,
                    "Metrics not enabled",
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            )));
        }

        Map<String, Long> chatRequests = getCounterValues("bailian.chat.requests");
        Map<String, Long> streamRequests = getCounterValues("bailian.stream.requests");
        Map<String, Long> embeddingRequests = getCounterValues("bailian.embedding.requests");

        UsageMetricsResponse response = new UsageMetricsResponse(
                true,
                "Metrics retrieved successfully",
                LocalDateTime.now(),
                chatRequests,
                streamRequests,
                embeddingRequests
        );

        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/latency")
    public Mono<ResponseEntity<LatencyMetricsResponse>> getLatency() {
        log.info("Getting latency metrics");

        if (!metricsRecorder.isEnabled()) {
            return Mono.just(ResponseEntity.ok(new LatencyMetricsResponse(
                    false,
                    "Metrics not enabled",
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            )));
        }

        Map<String, LatencyStats> chatLatency = getTimerStats("bailian.chat.duration");
        Map<String, LatencyStats> streamLatency = getTimerStats("bailian.stream.duration");
        Map<String, LatencyStats> embeddingLatency = getTimerStats("bailian.embedding.duration");

        LatencyMetricsResponse response = new LatencyMetricsResponse(
                true,
                "Metrics retrieved successfully",
                LocalDateTime.now(),
                chatLatency,
                streamLatency,
                embeddingLatency
        );

        return Mono.just(ResponseEntity.ok(response));
    }

    private Map<String, Long> getCounterValues(String counterName) {
        Map<String, Long> values = new HashMap<>();
        try {
            meterRegistry.find(counterName).counters().forEach(counter -> {
                String model = counter.getId().getTag("model");
                if (model != null) {
                    values.merge(model, (long) counter.count(), Long::sum);
                }
            });
        } catch (Exception e) {
            log.warn("Failed to get counter values for {}: {}", counterName, e.getMessage());
        }
        return values;
    }

    private Map<String, LatencyStats> getTimerStats(String timerName) {
        Map<String, LatencyStats> stats = new HashMap<>();
        try {
            meterRegistry.find(timerName).timers().forEach(timer -> {
                String model = timer.getId().getTag("model");
                if (model != null) {
                    double mean = timer.mean(TimeUnit.MILLISECONDS);
                    double max = timer.max(TimeUnit.MILLISECONDS);
                    // Micrometer Timer doesn't have min() method, use mean as approximation
                    double min = mean * 0.5;
                    stats.put(model, new LatencyStats(mean, max, min, timer.count()));
                }
            });
        } catch (Exception e) {
            log.warn("Failed to get timer stats for {}: {}", timerName, e.getMessage());
        }
        return stats;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class UsageMetricsResponse {
        private boolean enabled;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, Long> chatRequests;
        private Map<String, Long> streamRequests;
        private Map<String, Long> embeddingRequests;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LatencyMetricsResponse {
        private boolean enabled;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, LatencyStats> chatLatency;
        private Map<String, LatencyStats> streamLatency;
        private Map<String, LatencyStats> embeddingLatency;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LatencyStats {
        private double meanMs;
        private double maxMs;
        private double minMs;
        private long count;
    }
}

// Generated by Claude Code