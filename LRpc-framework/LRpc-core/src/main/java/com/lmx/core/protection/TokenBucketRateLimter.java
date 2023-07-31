package com.lmx.core.protection;


/**
 * 基于令牌桶算法的限流器
 */
public class TokenBucketRateLimter {
    private final int capacity;          // 令牌桶容量
    private final double refillRate;     // 令牌桶每秒填充速率
    private double tokens;               // 当前令牌数量
    private long lastRefillTimestamp;    // 上次填充令牌的时间戳

    public TokenBucketRateLimter(int capacity, double refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;  // 每秒添加几个令牌
        this.tokens = capacity;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public boolean allowRequest() {
        synchronized (this) {
            refillTokens();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }
    }

    private void refillTokens() {

            long now = System.currentTimeMillis();
            double elapsedTime = (now - lastRefillTimestamp) / 1000.0; // 转换为秒
//            大于一秒进行添加
            if (elapsedTime > 1) {
                double tokensToAdd = elapsedTime * refillRate;
                tokens = Math.min(tokens + tokensToAdd, capacity);
                lastRefillTimestamp = now;
            }


    }

    public static void main(String[] args) {
        final TokenBucketRateLimter tokenBucketRateLimter = new TokenBucketRateLimter(100, 1);
        for (int i = 0; i < 1000; i++) {
            final boolean b = tokenBucketRateLimter.allowRequest();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("b = " + b);
        }
    }
}

