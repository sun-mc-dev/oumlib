package dev.oum.oumlib.cooldown;

import dev.oum.oumlib.cooldown.store.CooldownStore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class CooldownManager<K> {

    private final Map<K, Cooldown<K>> cache = new ConcurrentHashMap<>();
    private final List<BiConsumer<K, Cooldown<K>>> expireListeners = new ArrayList<>();
    private Predicate<K> bypassPredicate = key -> false;
    private CooldownFormatter defaultFormatter = CooldownFormatter.DEFAULT;
    private CooldownStore<K> store;

    protected CooldownManager() {
    }

    public static <K> @NonNull CooldownManager<K> create() {
        return new CooldownManager<>();
    }

    public @NonNull CooldownManager<K> bypassPredicate(@NonNull Predicate<K> predicate) {
        this.bypassPredicate = Objects.requireNonNull(predicate);
        return this;
    }

    public @NonNull CooldownManager<K> defaultFormatter(@NonNull CooldownFormatter formatter) {
        this.defaultFormatter = Objects.requireNonNull(formatter);
        return this;
    }

    public @NonNull CooldownManager<K> onExpire(@NonNull BiConsumer<K, Cooldown<K>> listener) {
        this.expireListeners.add(Objects.requireNonNull(listener));
        return this;
    }

    public @NonNull CooldownManager<K> store(@Nullable CooldownStore<K> store) {
        this.store = store;
        return this;
    }

    public @NonNull CompletableFuture<Void> loadAllFromStore() {
        if (store == null) {
            return CompletableFuture.completedFuture(null);
        }
        return store.loadAll().thenAccept(loaded -> {
            loaded.forEach((k, cd) -> {
                if (cd.isActive()) {
                    cache.put(k, cd);
                }
            });
        });
    }

    public boolean isBypassed(@NonNull K key) {
        return bypassPredicate.test(key);
    }

    public boolean isOnCooldown(@NonNull K key) {
        if (isBypassed(key)) return false;
        Cooldown<K> cd = cache.get(key);
        if (cd == null) return false;
        if (cd.isExpired()) {
            removeAndTriggerExpire(key, cd);
            return false;
        }
        return true;
    }

    public boolean test(@NonNull K key) {
        return isOnCooldown(key);
    }

    public boolean testAndApply(@NonNull K key, @NonNull Duration duration) {
        return testAndApply(key, duration, null);
    }

    public boolean testAndApply(@NonNull K key, @NonNull Duration duration, @Nullable Object metadata) {
        if (isBypassed(key)) return true;
        synchronized (this) {
            if (isOnCooldown(key)) {
                return false;
            }
            apply(key, duration, metadata);
            return true;
        }
    }

    public @NonNull Cooldown<K> apply(@NonNull K key, @NonNull Duration duration) {
        return apply(key, duration, null);
    }

    public @NonNull Cooldown<K> apply(@NonNull K key, @NonNull Duration duration, @Nullable Object metadata) {
        Cooldown<K> cd = Cooldown.of(key, duration, metadata);
        cache.put(key, cd);
        if (store != null) {
            store.save(key, cd);
        }
        return cd;
    }

    public @NonNull Optional<Cooldown<K>> get(@NonNull K key) {
        if (isBypassed(key)) return Optional.empty();
        Cooldown<K> cd = cache.get(key);
        if (cd == null) return Optional.empty();
        if (cd.isExpired()) {
            removeAndTriggerExpire(key, cd);
            return Optional.empty();
        }
        return Optional.of(cd);
    }

    public long remainingMillis(@NonNull K key) {
        return get(key).map(Cooldown::remainingMillis).orElse(0L);
    }

    public @NonNull Duration remainingDuration(@NonNull K key) {
        return get(key).map(Cooldown::remainingDuration).orElse(Duration.ZERO);
    }

    public @NonNull String formatRemaining(@NonNull K key) {
        return formatRemaining(key, defaultFormatter);
    }

    public @NonNull String formatRemaining(@NonNull K key, @NonNull CooldownFormatter formatter) {
        return get(key).map(cd -> cd.formatRemaining(formatter)).orElse("0s");
    }

    public boolean reset(@NonNull K key) {
        Cooldown<K> removed = cache.remove(key);
        if (store != null) {
            store.remove(key);
        }
        return removed != null;
    }

    public boolean extend(@NonNull K key, @NonNull Duration amount) {
        Cooldown<K> existing = cache.get(key);
        if (existing == null || existing.isExpired()) return false;
        Instant newExpire = existing.expireTime().plus(amount);
        Cooldown<K> updated = new Cooldown<>(key, existing.startTime(), newExpire, existing.metadata());
        cache.put(key, updated);
        if (store != null) {
            store.save(key, updated);
        }
        return true;
    }

    public boolean reduce(@NonNull K key, @NonNull Duration amount) {
        Cooldown<K> existing = cache.get(key);
        if (existing == null || existing.isExpired()) return false;
        Instant newExpire = existing.expireTime().minus(amount);
        if (Instant.now().isAfter(newExpire)) {
            removeAndTriggerExpire(key, existing);
            return true;
        }
        Cooldown<K> updated = new Cooldown<>(key, existing.startTime(), newExpire, existing.metadata());
        cache.put(key, updated);
        if (store != null) {
            store.save(key, updated);
        }
        return true;
    }

    public void cleanUp() {
        Iterator<Map.Entry<K, Cooldown<K>>> it = cache.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, Cooldown<K>> entry = it.next();
            if (entry.getValue().isExpired()) {
                it.remove();
                if (store != null) {
                    store.remove(entry.getKey());
                }
                for (BiConsumer<K, Cooldown<K>> listener : expireListeners) {
                    listener.accept(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public void clear() {
        cache.clear();
        if (store != null) {
            store.clear();
        }
    }

    public @NonNull Map<K, Cooldown<K>> asMap() {
        return Collections.unmodifiableMap(cache);
    }

    private void removeAndTriggerExpire(K key, Cooldown<K> cd) {
        cache.remove(key, cd);
        if (store != null) {
            store.remove(key);
        }
        for (BiConsumer<K, Cooldown<K>> listener : expireListeners) {
            listener.accept(key, cd);
        }
    }
}
