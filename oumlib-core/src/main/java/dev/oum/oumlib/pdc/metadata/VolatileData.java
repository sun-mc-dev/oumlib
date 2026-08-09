package dev.oum.oumlib.pdc.metadata;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.pdc.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class VolatileData implements Listener {

    private static final Map<Object, Map<NamespacedKey, Entry<?>>> STORE = new ConcurrentHashMap<>();
    private static boolean registered = false;

    private VolatileData() {
    }

    public static void initialize() {
        if (registered) return;
        registered = true;
        if (OumLib.isPaper()) {
            Bukkit.getPluginManager().registerEvents(new VolatileData(), OumLib.plugin());
        }
    }

    public static <P, C> void set(@NonNull Object target, @NonNull DataKey<P, C> key, @NonNull C value) {
        set(target, key, value, null);
    }

    public static <P, C> void set(@NonNull Object target, @NonNull DataKey<P, C> key, @NonNull C value, @Nullable Duration ttl) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        initialize();

        Instant expiresAt = ttl != null ? Instant.now().plus(ttl) : null;
        STORE.computeIfAbsent(target, t -> new ConcurrentHashMap<>())
                .put(key.key(), new Entry<>(value, expiresAt));
    }

    @CheckReturnValue
    @SuppressWarnings("unchecked")
    public static <P, C> @NonNull Optional<C> get(@NonNull Object target, @NonNull DataKey<P, C> key) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(key);
        Map<NamespacedKey, Entry<?>> targetMap = STORE.get(target);
        if (targetMap == null) return Optional.empty();

        Entry<?> entry = targetMap.get(key.key());
        if (entry == null) return Optional.empty();

        if (entry.isExpired()) {
            targetMap.remove(key.key());
            return Optional.empty();
        }

        return Optional.of((C) entry.value());
    }

    @CheckReturnValue
    public static <P, C> @NonNull C getOrDefault(@NonNull Object target, @NonNull DataKey<P, C> key, @NonNull C defaultValue) {
        return get(target, key).orElse(defaultValue);
    }

    @CheckReturnValue
    public static <P, C> boolean has(@NonNull Object target, @NonNull DataKey<P, C> key) {
        return get(target, key).isPresent();
    }

    public static <P, C> void remove(@NonNull Object target, @NonNull DataKey<P, C> key) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(key);
        Map<NamespacedKey, Entry<?>> targetMap = STORE.get(target);
        if (targetMap != null) {
            targetMap.remove(key.key());
        }
    }

    public static void clear(@NonNull Object target) {
        Objects.requireNonNull(target);
        STORE.remove(target);
    }

    public static void clearAll() {
        STORE.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(@NonNull PlayerQuitEvent event) {
        clear(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(@NonNull EntityDeathEvent event) {
        clear(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(@NonNull ChunkUnloadEvent event) {
        clear(event.getChunk());
    }

    private record Entry<T>(T value, Instant expiresAt) {
        boolean isExpired() {
            return expiresAt != null && Instant.now().isAfter(expiresAt);
        }
    }
}
