package dev.oum.oumlib.pdc;

import dev.oum.oumlib.OumLib;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PdcFlags {

    private static final Map<String, Integer> FLAG_INDICES = new ConcurrentHashMap<>();
    private static final Map<Integer, String> INDEX_FLAGS = new ConcurrentHashMap<>();
    private static int nextIndex = 0;

    private final PersistentDataContainer container;
    private final NamespacedKey key;

    public PdcFlags(@NonNull PersistentDataHolder holder) {
        this(holder.getPersistentDataContainer(), new NamespacedKey(OumLib.plugin(), "pdc_flags"));
    }

    public PdcFlags(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
        this(holder.getPersistentDataContainer(), key);
    }

    public PdcFlags(@NonNull PersistentDataContainer container, @NonNull NamespacedKey key) {
        this.container = Objects.requireNonNull(container);
        this.key = Objects.requireNonNull(key);
    }

    private static synchronized int indexFor(@NonNull String flag) {
        return FLAG_INDICES.computeIfAbsent(flag.toLowerCase(Locale.ROOT), f -> {
            if (nextIndex >= 64) {
                return Math.abs(f.hashCode()) % 64;
            }
            int idx = nextIndex++;
            INDEX_FLAGS.put(idx, f);
            return idx;
        });
    }

    private long bitmask() {
        Long val = container.get(key, PersistentDataType.LONG);
        return val != null ? val : 0L;
    }

    private void save(long mask) {
        if (mask == 0L) {
            container.remove(key);
        } else {
            container.set(key, PersistentDataType.LONG, mask);
        }
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcFlags add(String @NonNull ... flags) {
        long mask = bitmask();
        for (String flag : flags) {
            int idx = indexFor(flag);
            mask |= (1L << idx);
        }
        save(mask);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcFlags remove(String @NonNull ... flags) {
        long mask = bitmask();
        for (String flag : flags) {
            int idx = indexFor(flag);
            mask &= ~(1L << idx);
        }
        save(mask);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcFlags toggle(@NonNull String flag) {
        long mask = bitmask();
        int idx = indexFor(flag);
        mask ^= (1L << idx);
        save(mask);
        return this;
    }

    @CheckReturnValue
    public boolean has(@NonNull String flag) {
        long mask = bitmask();
        int idx = indexFor(flag);
        return (mask & (1L << idx)) != 0L;
    }

    @CheckReturnValue
    public @NonNull Set<String> all() {
        long mask = bitmask();
        Set<String> set = new HashSet<>();
        for (int i = 0; i < 64; i++) {
            if ((mask & (1L << i)) != 0L) {
                String name = INDEX_FLAGS.get(i);
                if (name != null) {
                    set.add(name);
                }
            }
        }
        return set;
    }

    @Contract(value = "-> this", mutates = "this")
    public @NonNull PdcFlags clear() {
        container.remove(key);
        return this;
    }
}
