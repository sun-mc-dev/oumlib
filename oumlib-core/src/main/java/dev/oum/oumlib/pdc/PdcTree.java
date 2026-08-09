package dev.oum.oumlib.pdc;

import dev.oum.oumlib.OumLib;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class PdcTree {

    private final PersistentDataContainer current;
    private final PdcTree parent;
    private final NamespacedKey keyInParent;

    public PdcTree(@NonNull PersistentDataContainer container) {
        this(container, null, null);
    }

    private PdcTree(@NonNull PersistentDataContainer container, @Nullable PdcTree parent, @Nullable NamespacedKey keyInParent) {
        this.current = Objects.requireNonNull(container);
        this.parent = parent;
        this.keyInParent = keyInParent;
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull PdcTree of(@NonNull PersistentDataHolder holder) {
        return new PdcTree(holder.getPersistentDataContainer());
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull PdcTree of(@NonNull PersistentDataContainer container) {
        return new PdcTree(container);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public @NonNull PdcTree branch(@NonNull String name) {
        return branch(new NamespacedKey(OumLib.plugin(), name));
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public @NonNull PdcTree branch(@NonNull NamespacedKey key) {
        PersistentDataContainer child = current.get(key, PersistentDataType.TAG_CONTAINER);
        if (child == null) {
            child = current.getAdapterContext().newPersistentDataContainer();
            current.set(key, PersistentDataType.TAG_CONTAINER, child);
        }
        return new PdcTree(child, this, key);
    }

    @CheckReturnValue
    public @NonNull PdcTree parent() {
        if (parent != null && keyInParent != null) {
            parent.current.set(keyInParent, PersistentDataType.TAG_CONTAINER, current);
            return parent;
        }
        return this;
    }

    @CheckReturnValue
    public @NonNull PdcTree root() {
        PdcTree node = this;
        while (node.parent != null) {
            node = node.parent();
        }
        return node;
    }

    @CheckReturnValue
    public @NonNull PersistentDataContainer container() {
        return current;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public <P, C> @NonNull PdcTree set(@NonNull DataKey<P, C> key, @NonNull C value) {
        current.set(key.key(), key.type(), value);
        saveToParent();
        return this;
    }

    @CheckReturnValue
    public <P, C> @NonNull Optional<C> get(@NonNull DataKey<P, C> key) {
        return Optional.ofNullable(current.get(key.key(), key.type()));
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcTree set(@NonNull String key, @NonNull String value) {
        return set(new NamespacedKey(OumLib.plugin(), key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcTree set(@NonNull NamespacedKey key, @NonNull String value) {
        current.set(key, PersistentDataType.STRING, value);
        saveToParent();
        return this;
    }

    @CheckReturnValue
    public @Nullable String get(@NonNull String key) {
        return current.get(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.STRING);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcTree setInt(@NonNull String key, int value) {
        current.set(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.INTEGER, value);
        saveToParent();
        return this;
    }

    @CheckReturnValue
    public int getIntOrDefault(@NonNull String key, int def) {
        Integer val = current.get(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.INTEGER);
        return val != null ? val : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcTree setDouble(@NonNull String key, double value) {
        current.set(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.DOUBLE, value);
        saveToParent();
        return this;
    }

    @CheckReturnValue
    public double getDoubleOrDefault(@NonNull String key, double def) {
        Double val = current.get(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.DOUBLE);
        return val != null ? val : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcTree setBoolean(@NonNull String key, boolean value) {
        current.set(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.BYTE, (byte) (value ? 1 : 0));
        saveToParent();
        return this;
    }

    @CheckReturnValue
    public boolean getBooleanOrDefault(@NonNull String key, boolean def) {
        Byte val = current.get(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.BYTE);
        return val != null ? val != 0 : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcTree setLong(@NonNull String key, long value) {
        current.set(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.LONG, value);
        saveToParent();
        return this;
    }

    @CheckReturnValue
    public long getLongOrDefault(@NonNull String key, long def) {
        Long val = current.get(new NamespacedKey(OumLib.plugin(), key), PersistentDataType.LONG);
        return val != null ? val : def;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcTree remove(@NonNull String key) {
        current.remove(new NamespacedKey(OumLib.plugin(), key));
        saveToParent();
        return this;
    }

    @CheckReturnValue
    public boolean has(@NonNull String key) {
        return current.has(new NamespacedKey(OumLib.plugin(), key));
    }

    private void saveToParent() {
        if (parent != null && keyInParent != null) {
            parent.current.set(keyInParent, PersistentDataType.TAG_CONTAINER, current);
            parent.saveToParent();
        }
    }
}
