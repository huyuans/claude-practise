package com.huyuans.bailian.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TokenBucketRateLimiter.
 */
class TokenBucketRateLimiterTest {

    private TokenBucketRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = TokenBucketRateLimiter.create(10, 10);
    }

    @Test
    void testTryAcquire() {
        assertTrue(rateLimiter.tryAcquire());
        assertEquals(9, rateLimiter.getAvailableTokens());
    }

    @Test
    void testTryAcquireMultiple() {
        assertTrue(rateLimiter.tryAcquire(5));
        assertEquals(5, rateLimiter.getAvailableTokens());
    }

    @Test
    void testTryAcquireWhenInsufficient() {
        rateLimiter.tryAcquire(10);
        assertFalse(rateLimiter.tryAcquire());
    }

    @Test
    void testCapacity() {
        assertEquals(10, rateLimiter.getCapacity());
    }

    @Test
    void testRate() {
        assertEquals(10.0, rateLimiter.getRate(), 0.1);
    }

    @Test
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new TokenBucketRateLimiter(0, 10, 1, TimeUnit.SECONDS));
    }
}
