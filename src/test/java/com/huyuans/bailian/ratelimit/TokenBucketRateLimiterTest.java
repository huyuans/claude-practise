package com.huyuans.bailian.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("令牌桶限流器测试")
class TokenBucketRateLimiterTest {

    private TokenBucketRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = TokenBucketRateLimiter.create(10, 10);
    }

    @Test
    @DisplayName("基本获取令牌测试")
    void testTryAcquire() {
        assertTrue(rateLimiter.tryAcquire());
        assertEquals(9, rateLimiter.getAvailableTokens());
    }

    @Test
    @DisplayName("获取多个令牌测试")
    void testTryAcquireMultiple() {
        assertTrue(rateLimiter.tryAcquire(5));
        assertEquals(5, rateLimiter.getAvailableTokens());
    }

    @Test
    @DisplayName("令牌不足时获取失败")
    void testTryAcquireWhenInsufficient() {
        rateLimiter.tryAcquire(10);
        assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    @DisplayName("容量验证测试")
    void testCapacity() {
        assertEquals(10, rateLimiter.getCapacity());
    }

    @Test
    @DisplayName("速率验证测试")
    void testRate() {
        double rate = rateLimiter.getRate();
        assertEquals(10.0, rate, 0.1);
    }

    @Test
    @DisplayName("构造函数参数验证")
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class, 
                () -> new TokenBucketRateLimiter(0, 10, 1, TimeUnit.SECONDS));
    }
}