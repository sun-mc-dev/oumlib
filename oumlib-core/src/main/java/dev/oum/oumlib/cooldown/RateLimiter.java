package dev.oum.oumlib.cooldown;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimiter<K> {

    private final Strategy strategy;
    private final int maxPermits;
    private final long windowMillis;
    private final Map<K, WindowState> windowMap;
    private final Map<K, BucketState> bucketMap;

    private RateLimiter(Strategy strategy, int maxPermits, @NonNull Duration duration) {
        this.strategy = strategy;
        this.maxPermits = Math.max(1, maxPermits);
        this.windowMillis = Math.max(1, duration.toMillis());
        this.windowMap = (strategy == Strategy.SLIDING_WINDOW) ? new ConcurrentHashMap<>() : null;
        this.bucketMap = (strategy == Strategy.TOKEN_BUCKET) ? new ConcurrentHashMap<>() : null;
    }

    public static <K> @NonNull RateLimiter<K> slidingWindow(int maxRequests, @NonNull Duration windowDuration) {
        return new RateLimiter<>(Strategy.SLIDING_WINDOW, maxRequests, windowDuration);
    }

    public static <K> @NonNull RateLimiter<K> tokenBucket(int capacity, @NonNull Duration refillDuration) {
        return new RateLimiter<>(Strategy.TOKEN_BUCKET, capacity, refillDuration);
    }

    public boolean tryAcquire(@NonNull K key) {
        return tryAcquire(key, 1);
    }

    public boolean tryAcquire(@NonNull K key, int permits) {
        if (permits <= 0) return true;
        Objects.requireNonNull(key);

        if (strategy == Strategy.SLIDING_WINDOW) {
            return tryAcquireSlidingWindow(key, permits);
        } else {
            return tryAcquireTokenBucket(key, permits);
        }
    }

    private synchronized boolean tryAcquireSlidingWindow(K key, int permits) {
        long now = System.currentTimeMillis();
        long cutoff = now - windowMillis;
        WindowState state = windowMap.computeIfAbsent(key, k -> new WindowState());
        while (!state.timestamps.isEmpty() && state.timestamps.peekFirst() <= cutoff) {
            state.timestamps.pollFirst();
        }
        if (state.timestamps.size() + permits <= maxPermits) {
            for (int i = 0; i < permits; i++) {
                state.timestamps.addLast(now);
            }
            return true;
        }
        return false;
    }

    private synchronized boolean tryAcquireTokenBucket(K key, int permits) {
        long now = System.currentTimeMillis();
        BucketState state = bucketMap.computeIfAbsent(key, k -> new BucketState(maxPermits, now));
        long elapsed = now - state.lastRefill;
        if (elapsed > 0) {
            double tokensToAdd = ((double) elapsed / (double) windowMillis) * maxPermits;
            state.tokens = Math.min((double) maxPermits, state.tokens + tokensToAdd);
            state.lastRefill = now;
        }
        if (state.tokens >= permits) {
            state.tokens -= permits;
            return true;
        }
        return false;
    }

    public synchronized int availablePermits(@NonNull K key) {
        Objects.requireNonNull(key);
        long now = System.currentTimeMillis();
        if (strategy == Strategy.SLIDING_WINDOW) {
            WindowState state = windowMap.get(key);
            if (state == null) return maxPermits;
            long cutoff = now - windowMillis;
            while (!state.timestamps.isEmpty() && state.timestamps.peekFirst() <= cutoff) {
                state.timestamps.pollFirst();
            }
            return Math.max(0, maxPermits - state.timestamps.size());
        } else {
            BucketState state = bucketMap.get(key);
            if (state == null) return maxPermits;
            long elapsed = now - state.lastRefill;
            double tokens = state.tokens;
            if (elapsed > 0) {
                double tokensToAdd = ((double) elapsed / (double) windowMillis) * maxPermits;
                tokens = Math.min((double) maxPermits, tokens + tokensToAdd);
            }
            return (int) Math.floor(tokens);
        }
    }

    public void reset(@NonNull K key) {
        if (windowMap != null) windowMap.remove(key);
        if (bucketMap != null) bucketMap.remove(key);
    }

    public void clear() {
        if (windowMap != null) windowMap.clear();
        if (bucketMap != null) bucketMap.clear();
    }

    private enum Strategy {
        SLIDING_WINDOW,
        TOKEN_BUCKET
    }

    private static final class WindowState {
        private final Deque<Long> timestamps = new ArrayDeque<>();
    }

    private static final class BucketState {
        private double tokens;
        private long lastRefill;

        private BucketState(double tokens, long lastRefill) {
            this.tokens = tokens;
            this.lastRefill = lastRefill;
        }
    }
}
