package com.huyuans.bailian.ratelimit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class TokenBucketRateLimiter implements RateLimiter {

    private static final Logger log = Logger.getLogger(TokenBucketRateLimiter.class.getName());

    private final long capacity;
    private final long refillTokens;
    private final long refillPeriodMillis;
    private final AtomicLong availableTokens;
    private final AtomicLong lastRefillTime;
    private final ReentrantLock lock = new ReentrantLock();

    public TokenBucketRateLimiter(long capacity, long refillTokens, long refillPeriod, TimeUnit timeUnit) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("容量必须大于0");
        }
        if (refillTokens <= 0) {
            throw new IllegalArgumentException("补充令牌数必须大于0");
        }
        if (refillPeriod <= 0) {
            throw new IllegalArgumentException("补充周期必须大于0");
        }

        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriodMillis = timeUnit.toMillis(refillPeriod);
        this.availableTokens = new AtomicLong(capacity);
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());

        log.info("令牌桶限流器初始化: 容量=" + capacity + ", 补充令牌=" + refillTokens + "个/" + refillPeriodMillis + "ms");
    }

    public static TokenBucketRateLimiter create(long requestsPerSecond) {
        return new TokenBucketRateLimiter(requestsPerSecond, requestsPerSecond, 1, TimeUnit.SECONDS);
    }

    public static TokenBucketRateLimiter create(long capacity, long requestsPerSecond) {
        return new TokenBucketRateLimiter(capacity, requestsPerSecond, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    @Override
    public boolean tryAcquire(int permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("请求数必须大于0");
        }

        lock.lock();
        try {
            refill();

            long current = availableTokens.get();
            if (current >= permits) {
                availableTokens.set(current - permits);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return tryAcquire(1, timeout, unit);
    }

    @Override
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        if (permits <= 0) {
            throw new IllegalArgumentException("请求数必须大于0");
        }

        long timeoutMillis = unit.toMillis(timeout);
        long startTime = System.currentTimeMillis();

        while (true) {
            if (tryAcquire(permits)) {
                return true;
            }

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeoutMillis) {
                return false;
            }

            long waitTime = Math.min(100, timeoutMillis - elapsed);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    @Override
    public void acquire() throws InterruptedException {
        acquire(1);
    }

    @Override
    public void acquire(int permits) throws InterruptedException {
        while (!tryAcquire(permits)) {
            Thread.sleep(calculateWaitTime());
        }
    }

    @Override
    public long getAvailableTokens() {
        lock.lock();
        try {
            refill();
            return availableTokens.get();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public double getRate() {
        return (double) refillTokens / refillPeriodMillis * 1000;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long lastRefill = lastRefillTime.get();

        long elapsed = now - lastRefill;
        if (elapsed < refillPeriodMillis) {
            return;
        }

        long refillCycles = elapsed / refillPeriodMillis;
        long tokensToAdd = refillCycles * refillTokens;

        long currentTokens = availableTokens.get();
        long newTokens = Math.min(capacity, currentTokens + tokensToAdd);

        availableTokens.set(newTokens);
        lastRefillTime.set(now);
    }

    private long calculateWaitTime() {
        long available = getAvailableTokens();
        if (available > 0) {
            return 0;
        }
        return refillPeriodMillis;
    }

    @Override
    public String toString() {
        return String.format("TokenBucketRateLimiter{capacity=%d, rate=%.2f/s, available=%d}",
                capacity, getRate(), getAvailableTokens());
    }
}