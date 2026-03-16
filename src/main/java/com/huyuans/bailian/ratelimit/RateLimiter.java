package com.huyuans.bailian.ratelimit;

import java.util.concurrent.TimeUnit;

public interface RateLimiter {

    boolean tryAcquire();

    boolean tryAcquire(int permits);

    boolean tryAcquire(long timeout, TimeUnit unit);

    boolean tryAcquire(int permits, long timeout, TimeUnit unit);

    void acquire() throws InterruptedException;

    void acquire(int permits) throws InterruptedException;

    long getAvailableTokens();

    long getCapacity();

    double getRate();
}