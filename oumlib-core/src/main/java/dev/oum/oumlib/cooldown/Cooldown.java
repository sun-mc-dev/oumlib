package dev.oum.oumlib.cooldown;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

public record Cooldown<K>(
        @NonNull K key,
        @NonNull Instant startTime,
        @NonNull Instant expireTime,
        @Nullable Object metadata
) {

    public Cooldown(@NonNull K key, @NonNull Instant startTime, @NonNull Instant expireTime) {
        this(key, startTime, expireTime, null);
    }

    public static <K> @NonNull Cooldown<K> of(@NonNull K key, @NonNull Duration duration) {
        Instant now = Instant.now();
        return new Cooldown<>(key, now, now.plus(duration));
    }

    public static <K> @NonNull Cooldown<K> of(@NonNull K key, @NonNull Duration duration, @Nullable Object metadata) {
        Instant now = Instant.now();
        return new Cooldown<>(key, now, now.plus(duration), metadata);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expireTime);
    }

    public boolean isActive() {
        return !isExpired();
    }

    public long remainingMillis() {
        long diff = expireTime.toEpochMilli() - System.currentTimeMillis();
        return Math.max(0L, diff);
    }

    public @NonNull Duration remainingDuration() {
        return Duration.ofMillis(remainingMillis());
    }

    public long totalDurationMillis() {
        return Math.max(0L, expireTime.toEpochMilli() - startTime.toEpochMilli());
    }

    public @NonNull Duration totalDuration() {
        return Duration.ofMillis(totalDurationMillis());
    }

    public double progress() {
        long total = totalDurationMillis();
        if (total <= 0) return 1.0;
        long elapsed = System.currentTimeMillis() - startTime.toEpochMilli();
        return Math.clamp((double) elapsed / (double) total, 0.0, 1.0);
    }

    public @NonNull String formatRemaining() {
        return CooldownFormatter.DEFAULT.format(remainingDuration());
    }

    public @NonNull String formatRemaining(@NonNull CooldownFormatter formatter) {
        return formatter.format(remainingDuration());
    }
}
