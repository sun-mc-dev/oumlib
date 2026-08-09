package dev.oum.oumlib.pdc;

import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.inventory.ItemSerializer;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class PdcItem {

    private static final Gson GSON = new Gson();

    private final ItemStack item;
    private final String prefix;

    public PdcItem(@NonNull ItemStack item) {
        this(item, null);
    }

    public PdcItem(@NonNull ItemStack item, @Nullable String prefix) {
        this.item = item;
        this.prefix = prefix;
    }

    @Contract(value = "_ -> new", pure = true)
    @CheckReturnValue
    public @NonNull PdcItem namespaced(@NonNull String subNamespace) {
        return new PdcItem(item, prefix == null ? subNamespace : prefix + "_" + subNamespace);
    }

    @CheckReturnValue
    public @NonNull NamespacedKey nsk(@NonNull String key) {
        String finalKey = prefix == null ? key : prefix + "_" + key;
        return new NamespacedKey(OumLib.plugin(), finalKey);
    }

    @CheckReturnValue
    public @NonNull ItemStack item() {
        return item;
    }

    private boolean updateMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        consumer.accept(meta);
        return item.setItemMeta(meta);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public <P, C> @NonNull PdcItem set(@NonNull DataKey<P, C> key, @Nullable C value) {
        C oldValue = get(key).orElse(null);
        updateMeta(meta -> {
            if (value == null) {
                meta.getPersistentDataContainer().remove(key.key());
            } else {
                meta.getPersistentDataContainer().set(key.key(), key.type(), value);
            }
        });
        PDC.triggerListeners(item, key.key(), oldValue, value);
        return this;
    }

    @CheckReturnValue
    public <P, C> @NonNull Optional<C> get(@NonNull DataKey<P, C> key) {
        if (!item.hasItemMeta()) return Optional.empty();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();
        return Optional.ofNullable(meta.getPersistentDataContainer().get(key.key(), key.type()));
    }

    @CheckReturnValue
    public <P, C> @NonNull C getOrDefault(@NonNull DataKey<P, C> key, @NonNull C def) {
        return get(key).orElse(def);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem set(@NonNull String key, @Nullable String value) {
        return set(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem set(@NonNull NamespacedKey key, @Nullable String value) {
        String oldValue = get(key);
        updateMeta(meta -> {
            if (value == null) {
                meta.getPersistentDataContainer().remove(key);
            } else {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
            }
        });
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable String get(@NonNull String key) {
        return get(nsk(key));
    }

    @CheckReturnValue
    public @Nullable String get(@NonNull NamespacedKey key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    @CheckReturnValue
    public @NonNull String getOrDefault(@NonNull String key, @NonNull String def) {
        return getOrDefault(nsk(key), def);
    }

    @CheckReturnValue
    public @NonNull String getOrDefault(@NonNull NamespacedKey key, @NonNull String def) {
        String val = get(key);
        return val != null ? val : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setInt(@NonNull String key, int value) {
        return setInt(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setInt(@NonNull NamespacedKey key, int value) {
        Integer oldValue = getInt(key);
        updateMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value));
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable Integer getInt(@NonNull String key) {
        return getInt(nsk(key));
    }

    @CheckReturnValue
    public @Nullable Integer getInt(@NonNull NamespacedKey key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }

    @CheckReturnValue
    public int getIntOrDefault(@NonNull String key, int def) {
        return getIntOrDefault(nsk(key), def);
    }

    @CheckReturnValue
    public int getIntOrDefault(@NonNull NamespacedKey key, int def) {
        Integer val = getInt(key);
        return val != null ? val : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setDouble(@NonNull String key, double value) {
        return setDouble(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setDouble(@NonNull NamespacedKey key, double value) {
        Double oldValue = getDouble(key);
        updateMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value));
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable Double getDouble(@NonNull String key) {
        return getDouble(nsk(key));
    }

    @CheckReturnValue
    public @Nullable Double getDouble(@NonNull NamespacedKey key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
    }

    @CheckReturnValue
    public double getDoubleOrDefault(@NonNull String key, double def) {
        return getDoubleOrDefault(nsk(key), def);
    }

    @CheckReturnValue
    public double getDoubleOrDefault(@NonNull NamespacedKey key, double def) {
        Double val = getDouble(key);
        return val != null ? val : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setBoolean(@NonNull String key, boolean value) {
        return setBoolean(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setBoolean(@NonNull NamespacedKey key, boolean value) {
        Boolean oldValue = getBoolean(key);
        updateMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0)));
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public boolean getBoolean(@NonNull String key) {
        return getBoolean(nsk(key));
    }

    @CheckReturnValue
    public boolean getBoolean(@NonNull NamespacedKey key) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Byte b = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return b != null && b != 0;
    }

    @CheckReturnValue
    public boolean getBooleanOrDefault(@NonNull String key, boolean def) {
        return getBooleanOrDefault(nsk(key), def);
    }

    @CheckReturnValue
    public boolean getBooleanOrDefault(@NonNull NamespacedKey key, boolean def) {
        if (!item.hasItemMeta()) return def;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return def;
        Byte b = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return b != null ? b != 0 : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setLong(@NonNull String key, long value) {
        return setLong(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setLong(@NonNull NamespacedKey key, long value) {
        Long oldValue = getLong(key);
        updateMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.LONG, value));
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable Long getLong(@NonNull String key) {
        return getLong(nsk(key));
    }

    @CheckReturnValue
    public @Nullable Long getLong(@NonNull NamespacedKey key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.LONG);
    }

    @CheckReturnValue
    public long getLongOrDefault(@NonNull String key, long def) {
        return getLongOrDefault(nsk(key), def);
    }

    @CheckReturnValue
    public long getLongOrDefault(@NonNull NamespacedKey key, long def) {
        Long val = getLong(key);
        return val != null ? val : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setList(@NonNull String key, @Nullable List<String> value) {
        return setList(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setList(@NonNull NamespacedKey key, @Nullable List<String> value) {
        List<String> oldValue = getList(key);
        updateMeta(meta -> {
            if (value == null) {
                meta.getPersistentDataContainer().remove(key);
            } else {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, GSON.toJson(value));
            }
        });
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable List<String> getList(@NonNull String key) {
        return getList(nsk(key));
    }

    @CheckReturnValue
    public @Nullable List<String> getList(@NonNull NamespacedKey key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String raw = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (raw == null) return null;
        if (raw.isEmpty()) return List.of();
        return Arrays.asList(GSON.fromJson(raw, String[].class));
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setComponent(@NonNull String key, @Nullable Component value) {
        return setComponent(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setComponent(@NonNull NamespacedKey key, @Nullable Component value) {
        Component oldValue = getComponent(key);
        updateMeta(meta -> {
            if (value == null) {
                meta.getPersistentDataContainer().remove(key);
            } else {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, Text.serialize(value));
            }
        });
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable Component getComponent(@NonNull String key) {
        return getComponent(nsk(key));
    }

    @CheckReturnValue
    public @Nullable Component getComponent(@NonNull NamespacedKey key) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String val = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return val != null ? Text.parse(val) : null;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public <T> @NonNull PdcItem setObject(@NonNull String key, @Nullable T value) {
        return setObject(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public <T> @NonNull PdcItem setObject(@NonNull NamespacedKey key, @Nullable T value) {
        Object oldValue = get(key);
        updateMeta(meta -> {
            if (value == null) {
                meta.getPersistentDataContainer().remove(key);
            } else {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, GSON.toJson(value));
            }
        });
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public <T> @Nullable T getObject(@NonNull String key, @NonNull Class<T> type) {
        return getObject(nsk(key), type);
    }

    @CheckReturnValue
    public <T> @Nullable T getObject(@NonNull NamespacedKey key, @NonNull Class<T> type) {
        String raw = get(key);
        if (raw == null) return null;
        return GSON.fromJson(raw, type);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setItem(@NonNull String key, @Nullable ItemStack value) {
        return setItem(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setItem(@NonNull NamespacedKey key, @Nullable ItemStack value) {
        Object oldValue = get(key);
        updateMeta(meta -> {
            if (value == null) {
                meta.getPersistentDataContainer().remove(key);
            } else {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, ItemSerializer.serialize(value));
            }
        });
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable ItemStack getItem(@NonNull String key) {
        return getItem(nsk(key));
    }

    @CheckReturnValue
    public @Nullable ItemStack getItem(@NonNull NamespacedKey key) {
        String base64 = get(key);
        if (base64 == null) return null;
        return ItemSerializer.deserialize(base64);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setItemArray(@NonNull String key, ItemStack @Nullable [] value) {
        return setItemArray(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcItem setItemArray(@NonNull NamespacedKey key, ItemStack @Nullable [] value) {
        Object oldValue = get(key);
        updateMeta(meta -> {
            if (value == null) {
                meta.getPersistentDataContainer().remove(key);
            } else {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, ItemSerializer.serializeArray(value));
            }
        });
        PDC.triggerListeners(item, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public ItemStack @Nullable [] getItemArray(@NonNull String key) {
        return getItemArray(nsk(key));
    }

    @CheckReturnValue
    public ItemStack @Nullable [] getItemArray(@NonNull NamespacedKey key) {
        String base64 = get(key);
        if (base64 == null) return null;
        return ItemSerializer.deserializeArray(base64);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcItem remove(@NonNull String key) {
        return remove(nsk(key));
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcItem remove(@NonNull NamespacedKey key) {
        updateMeta(meta -> meta.getPersistentDataContainer().remove(key));
        PDC.triggerListeners(item, key, null, null);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcItem remove(@NonNull DataKey<?, ?> key) {
        updateMeta(meta -> meta.getPersistentDataContainer().remove(key.key()));
        PDC.triggerListeners(item, key.key(), null, null);
        return this;
    }

    @CheckReturnValue
    public boolean has(@NonNull String key) {
        return has(nsk(key));
    }

    @CheckReturnValue
    public boolean has(@NonNull NamespacedKey key) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(key);
    }

    @CheckReturnValue
    public boolean has(@NonNull DataKey<?, ?> key) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(key.key(), key.type());
    }
}
