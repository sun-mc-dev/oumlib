package dev.oum.oumlib.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PDC {

    private static final Map<NamespacedKey, List<PdcChangeListener>> listeners = new ConcurrentHashMap<>();

    private PDC() {
    }

    public static void registerListener(@NonNull NamespacedKey key, @NonNull PdcChangeListener listener) {
        listeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public static void registerListener(@NonNull DataKey<?, ?> key, @NonNull PdcChangeListener listener) {
        registerListener(key.key(), listener);
    }

    public static void unregisterListener(@NonNull NamespacedKey key, @NonNull PdcChangeListener listener) {
        List<PdcChangeListener> list = listeners.get(key);
        if (list != null) {
            list.remove(listener);
        }
    }

    public static void unregisterListener(@NonNull DataKey<?, ?> key, @NonNull PdcChangeListener listener) {
        unregisterListener(key.key(), listener);
    }

    public static void triggerListeners(@NonNull Object target, @NonNull NamespacedKey key,
                                        @Nullable Object oldValue, @Nullable Object newValue) {
        List<PdcChangeListener> list = listeners.get(key);
        if (list != null) {
            for (PdcChangeListener listener : list) {
                try {
                    listener.onChange(target, key, oldValue, newValue);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull PdcHolder of(@NonNull PersistentDataHolder holder) {
        return new PdcHolder(holder);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull PdcItem of(@NonNull ItemStack item) {
        return new PdcItem(item);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull PdcTree tree(@NonNull PersistentDataHolder holder) {
        return PdcTree.of(holder);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull PdcTree tree(@NonNull PersistentDataContainer container) {
        return PdcTree.of(container);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull PdcFlags flags(@NonNull PersistentDataHolder holder) {
        return new PdcFlags(holder);
    }

    @Contract("_, _ -> new")
    @CheckReturnValue
    public static @NonNull PdcFlags flags(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
        return new PdcFlags(holder, key);
    }

    @Contract("_, _ -> new")
    @CheckReturnValue
    public static <P, C> @NonNull PdcProperty<P, C> property(@NonNull PersistentDataHolder holder, @NonNull DataKey<P, C> key) {
        return new PdcProperty<>(holder, key, null);
    }

    @Contract("_, _, _ -> new")
    @CheckReturnValue
    public static <P, C> @NonNull PdcProperty<P, C> property(@NonNull PersistentDataHolder holder, @NonNull DataKey<P, C> key, @Nullable C defaultValue) {
        return new PdcProperty<>(holder, key, defaultValue);
    }

    public static <T extends Record> void write(@NonNull PersistentDataHolder holder, @NonNull T recordInstance) {
        PdcModel.write(holder.getPersistentDataContainer(), recordInstance);
    }

    @Contract("_, _ -> param1")
    public static <T extends Record> @NonNull ItemStack write(@NonNull ItemStack item, @NonNull T recordInstance) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PdcModel.write(meta.getPersistentDataContainer(), recordInstance);
            item.setItemMeta(meta);
        }
        return item;
    }

    @CheckReturnValue
    public static <T extends Record> @NonNull Optional<T> read(@NonNull PersistentDataHolder holder, @NonNull Class<T> recordClass) {
        return PdcModel.read(holder.getPersistentDataContainer(), recordClass);
    }

    @CheckReturnValue
    public static <T extends Record> @NonNull Optional<T> read(@NonNull ItemStack item, @NonNull Class<T> recordClass) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();
        return PdcModel.read(meta.getPersistentDataContainer(), recordClass);
    }

    public static <P, C> void set(@NonNull PersistentDataHolder holder, @NonNull DataKey<P, C> key, @NonNull C value) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        C oldValue = holder.getPersistentDataContainer().get(key.key(), key.type());
        holder.getPersistentDataContainer().set(key.key(), key.type(), value);
        triggerListeners(holder, key.key(), oldValue, value);
    }

    public static <P, C> void set(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, @NonNull PersistentDataType<P, C> type, @NonNull C value) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);
        C oldValue = holder.getPersistentDataContainer().get(key, type);
        holder.getPersistentDataContainer().set(key, type, value);
        triggerListeners(holder, key, oldValue, value);
    }

    @CheckReturnValue
    public static <P, C> @NonNull Optional<C> get(@NonNull PersistentDataHolder holder, @NonNull DataKey<P, C> key) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        return Optional.ofNullable(holder.getPersistentDataContainer().get(key.key(), key.type()));
    }

    @CheckReturnValue
    public static <P, C> @NonNull Optional<C> get(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, @NonNull PersistentDataType<P, C> type) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        Objects.requireNonNull(type);
        return Optional.ofNullable(holder.getPersistentDataContainer().get(key, type));
    }

    @CheckReturnValue
    public static <P, C> @NonNull C getOrDefault(@NonNull PersistentDataHolder holder, @NonNull DataKey<P, C> key, @NonNull C defaultValue) {
        return get(holder, key).orElse(defaultValue);
    }

    @CheckReturnValue
    public static <P, C> boolean has(@NonNull PersistentDataHolder holder, @NonNull DataKey<P, C> key) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        return holder.getPersistentDataContainer().has(key.key(), key.type());
    }

    @CheckReturnValue
    public static <P, C> boolean has(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key, @NonNull PersistentDataType<P, C> type) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        Objects.requireNonNull(type);
        return holder.getPersistentDataContainer().has(key, type);
    }

    @CheckReturnValue
    public static boolean has(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        return holder.getPersistentDataContainer().has(key);
    }

    public static void remove(@NonNull PersistentDataHolder holder, @NonNull DataKey<?, ?> key) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        holder.getPersistentDataContainer().remove(key.key());
        triggerListeners(holder, key.key(), null, null);
    }

    public static void remove(@NonNull PersistentDataHolder holder, @NonNull NamespacedKey key) {
        Objects.requireNonNull(holder);
        Objects.requireNonNull(key);
        holder.getPersistentDataContainer().remove(key);
        triggerListeners(holder, key, null, null);
    }

    @CheckReturnValue
    public static @NonNull Set<NamespacedKey> getKeys(@NonNull PersistentDataHolder holder) {
        Objects.requireNonNull(holder);
        return holder.getPersistentDataContainer().getKeys();
    }

    @Contract("_, _, _ -> param1")
    public static <P, C> @NonNull ItemStack set(@NonNull ItemStack item, @NonNull DataKey<P, C> key, @NonNull C value) {
        Objects.requireNonNull(item);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            C oldValue = meta.getPersistentDataContainer().get(key.key(), key.type());
            meta.getPersistentDataContainer().set(key.key(), key.type(), value);
            item.setItemMeta(meta);
            triggerListeners(item, key.key(), oldValue, value);
        }
        return item;
    }

    @Contract("_, _, _, _ -> param1")
    public static <P, C> @NonNull ItemStack set(@NonNull ItemStack item, @NonNull NamespacedKey key, @NonNull PersistentDataType<P, C> type, @NonNull C value) {
        Objects.requireNonNull(item);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            C oldValue = meta.getPersistentDataContainer().get(key, type);
            meta.getPersistentDataContainer().set(key, type, value);
            item.setItemMeta(meta);
            triggerListeners(item, key, oldValue, value);
        }
        return item;
    }

    @CheckReturnValue
    public static <P, C> @NonNull Optional<C> get(@NonNull ItemStack item, @NonNull DataKey<P, C> key) {
        Objects.requireNonNull(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();
        return get(meta, key);
    }

    @CheckReturnValue
    public static <P, C> @NonNull Optional<C> get(@NonNull ItemStack item, @NonNull NamespacedKey key, @NonNull PersistentDataType<P, C> type) {
        Objects.requireNonNull(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();
        return get(meta, key, type);
    }

    @CheckReturnValue
    public static <P, C> @NonNull C getOrDefault(@NonNull ItemStack item, @NonNull DataKey<P, C> key, @NonNull C defaultValue) {
        return get(item, key).orElse(defaultValue);
    }

    @CheckReturnValue
    public static <P, C> boolean has(@NonNull ItemStack item, @NonNull DataKey<P, C> key) {
        Objects.requireNonNull(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return has(meta, key);
    }

    @CheckReturnValue
    public static boolean has(@NonNull ItemStack item, @NonNull NamespacedKey key) {
        Objects.requireNonNull(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return has(meta, key);
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack remove(@NonNull ItemStack item, @NonNull DataKey<?, ?> key) {
        Objects.requireNonNull(item);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            remove(meta, key);
            item.setItemMeta(meta);
            triggerListeners(item, key.key(), null, null);
        }
        return item;
    }

    public static <P, C> void set(@NonNull BlockState blockState, @NonNull DataKey<P, C> key, @NonNull C value) {
        if (blockState instanceof TileState tileState) {
            set((PersistentDataHolder) tileState, key, value);
            tileState.update();
        }
    }

    @CheckReturnValue
    public static <P, C> @NonNull Optional<C> get(@NonNull BlockState blockState, @NonNull DataKey<P, C> key) {
        if (blockState instanceof TileState tileState) {
            return get((PersistentDataHolder) tileState, key);
        }
        return Optional.empty();
    }

    @CheckReturnValue
    public static @NonNull PersistentDataContainer container(@NonNull PersistentDataHolder holder) {
        return holder.getPersistentDataContainer();
    }

    @CheckReturnValue
    public static @Nullable PersistentDataContainer container(@NonNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null ? meta.getPersistentDataContainer() : null;
    }
}
