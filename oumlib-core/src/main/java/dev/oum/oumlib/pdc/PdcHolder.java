package dev.oum.oumlib.pdc;

import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.inventory.ItemSerializer;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class PdcHolder {

    private static final Gson GSON = new Gson();

    private final PersistentDataHolder holder;
    private final PersistentDataContainer pdc;
    private final String prefix;

    public PdcHolder(@NonNull PersistentDataHolder holder) {
        this(holder, null);
    }

    public PdcHolder(@NonNull PersistentDataHolder holder, @Nullable String prefix) {
        this.holder = holder;
        this.pdc = holder.getPersistentDataContainer();
        this.prefix = prefix;
    }

    @Contract(value = "_ -> new", pure = true)
    @CheckReturnValue
    public @NonNull PdcHolder namespaced(@NonNull String subNamespace) {
        return new PdcHolder(holder, prefix == null ? subNamespace : prefix + "_" + subNamespace);
    }

    @CheckReturnValue
    public @NonNull NamespacedKey nsk(@NonNull String key) {
        String finalKey = prefix == null ? key : prefix + "_" + key;
        return new NamespacedKey(OumLib.plugin(), finalKey);
    }

    @CheckReturnValue
    public @NonNull PersistentDataHolder holder() {
        return holder;
    }

    @CheckReturnValue
    public @NonNull PersistentDataContainer container() {
        return pdc;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public <P, C> @NonNull PdcHolder set(@NonNull DataKey<P, C> key, @Nullable C value) {
        C oldValue = pdc.get(key.key(), key.type());
        if (value == null) {
            pdc.remove(key.key());
        } else {
            pdc.set(key.key(), key.type(), value);
        }
        PDC.triggerListeners(holder, key.key(), oldValue, value);
        return this;
    }

    @CheckReturnValue
    public <P, C> @NonNull Optional<C> get(@NonNull DataKey<P, C> key) {
        return Optional.ofNullable(pdc.get(key.key(), key.type()));
    }

    @CheckReturnValue
    public <P, C> @NonNull C getOrDefault(@NonNull DataKey<P, C> key, @NonNull C def) {
        C val = pdc.get(key.key(), key.type());
        return val != null ? val : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder set(@NonNull String key, @Nullable String value) {
        return set(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder set(@NonNull NamespacedKey key, @Nullable String value) {
        String oldValue = pdc.get(key, PersistentDataType.STRING);
        if (value == null) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.STRING, value);
        }
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable String get(@NonNull String key) {
        return get(nsk(key));
    }

    @CheckReturnValue
    public @Nullable String get(@NonNull NamespacedKey key) {
        return pdc.get(key, PersistentDataType.STRING);
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
    public @NonNull PdcHolder setInt(@NonNull String key, int value) {
        return setInt(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setInt(@NonNull NamespacedKey key, int value) {
        Integer oldValue = pdc.get(key, PersistentDataType.INTEGER);
        pdc.set(key, PersistentDataType.INTEGER, value);
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable Integer getInt(@NonNull String key) {
        return getInt(nsk(key));
    }

    @CheckReturnValue
    public @Nullable Integer getInt(@NonNull NamespacedKey key) {
        return pdc.get(key, PersistentDataType.INTEGER);
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
    public @NonNull PdcHolder setDouble(@NonNull String key, double value) {
        return setDouble(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setDouble(@NonNull NamespacedKey key, double value) {
        Double oldValue = pdc.get(key, PersistentDataType.DOUBLE);
        pdc.set(key, PersistentDataType.DOUBLE, value);
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable Double getDouble(@NonNull String key) {
        return getDouble(nsk(key));
    }

    @CheckReturnValue
    public @Nullable Double getDouble(@NonNull NamespacedKey key) {
        return pdc.get(key, PersistentDataType.DOUBLE);
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
    public @NonNull PdcHolder setBoolean(@NonNull String key, boolean value) {
        return setBoolean(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setBoolean(@NonNull NamespacedKey key, boolean value) {
        Byte b = pdc.get(key, PersistentDataType.BYTE);
        Boolean oldValue = b != null ? b != 0 : null;
        pdc.set(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public boolean getBoolean(@NonNull String key) {
        return getBoolean(nsk(key));
    }

    @CheckReturnValue
    public boolean getBoolean(@NonNull NamespacedKey key) {
        Byte b = pdc.get(key, PersistentDataType.BYTE);
        return b != null && b != 0;
    }

    @CheckReturnValue
    public boolean getBooleanOrDefault(@NonNull String key, boolean def) {
        return getBooleanOrDefault(nsk(key), def);
    }

    @CheckReturnValue
    public boolean getBooleanOrDefault(@NonNull NamespacedKey key, boolean def) {
        Byte b = pdc.get(key, PersistentDataType.BYTE);
        return b != null ? b != 0 : def;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setLong(@NonNull String key, long value) {
        return setLong(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setLong(@NonNull NamespacedKey key, long value) {
        Long oldValue = pdc.get(key, PersistentDataType.LONG);
        pdc.set(key, PersistentDataType.LONG, value);
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable Long getLong(@NonNull String key) {
        return getLong(nsk(key));
    }

    @CheckReturnValue
    public @Nullable Long getLong(@NonNull NamespacedKey key) {
        return pdc.get(key, PersistentDataType.LONG);
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
    public @NonNull PdcHolder setComponent(@NonNull String key, @Nullable Component value) {
        return setComponent(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setComponent(@NonNull NamespacedKey key, @Nullable Component value) {
        Component oldValue = getComponent(key);
        if (value == null) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.STRING, Text.serialize(value));
        }
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable Component getComponent(@NonNull String key) {
        return getComponent(nsk(key));
    }

    @CheckReturnValue
    public @Nullable Component getComponent(@NonNull NamespacedKey key) {
        String val = pdc.get(key, PersistentDataType.STRING);
        return val != null ? Text.parse(val) : null;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setList(@NonNull String key, @Nullable List<String> value) {
        return setList(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setList(@NonNull NamespacedKey key, @Nullable List<String> value) {
        List<String> oldValue = getList(key);
        if (value == null) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.STRING, GSON.toJson(value));
        }
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable List<String> getList(@NonNull String key) {
        return getList(nsk(key));
    }

    @CheckReturnValue
    public @Nullable List<String> getList(@NonNull NamespacedKey key) {
        String raw = pdc.get(key, PersistentDataType.STRING);
        if (raw == null) return null;
        if (raw.isEmpty()) return List.of();
        return Arrays.asList(GSON.fromJson(raw, String[].class));
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public <T> @NonNull PdcHolder setObject(@NonNull String key, @Nullable T value) {
        return setObject(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public <T> @NonNull PdcHolder setObject(@NonNull NamespacedKey key, @Nullable T value) {
        Object oldValue = pdc.get(key, PersistentDataType.STRING);
        if (value == null) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.STRING, GSON.toJson(value));
        }
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public <T> @Nullable T getObject(@NonNull String key, @NonNull Class<T> type) {
        return getObject(nsk(key), type);
    }

    @CheckReturnValue
    public <T> @Nullable T getObject(@NonNull NamespacedKey key, @NonNull Class<T> type) {
        String raw = pdc.get(key, PersistentDataType.STRING);
        if (raw == null) return null;
        return GSON.fromJson(raw, type);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setItem(@NonNull String key, @Nullable ItemStack value) {
        return setItem(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setItem(@NonNull NamespacedKey key, @Nullable ItemStack value) {
        String oldValue = pdc.get(key, PersistentDataType.STRING);
        if (value == null) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.STRING, ItemSerializer.serialize(value));
        }
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public @Nullable ItemStack getItem(@NonNull String key) {
        return getItem(nsk(key));
    }

    @CheckReturnValue
    public @Nullable ItemStack getItem(@NonNull NamespacedKey key) {
        String base64 = pdc.get(key, PersistentDataType.STRING);
        if (base64 == null) return null;
        return ItemSerializer.deserialize(base64);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setItemArray(@NonNull String key, ItemStack @Nullable [] value) {
        return setItemArray(nsk(key), value);
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull PdcHolder setItemArray(@NonNull NamespacedKey key, ItemStack @Nullable [] value) {
        String oldValue = pdc.get(key, PersistentDataType.STRING);
        if (value == null) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.STRING, ItemSerializer.serializeArray(value));
        }
        PDC.triggerListeners(holder, key, oldValue, value);
        return this;
    }

    @CheckReturnValue
    public ItemStack @Nullable [] getItemArray(@NonNull String key) {
        return getItemArray(nsk(key));
    }

    @CheckReturnValue
    public ItemStack @Nullable [] getItemArray(@NonNull NamespacedKey key) {
        String base64 = pdc.get(key, PersistentDataType.STRING);
        if (base64 == null) return null;
        return ItemSerializer.deserializeArray(base64);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcHolder remove(@NonNull String key) {
        return remove(nsk(key));
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcHolder remove(@NonNull NamespacedKey key) {
        pdc.remove(key);
        PDC.triggerListeners(holder, key, null, null);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull PdcHolder remove(@NonNull DataKey<?, ?> key) {
        pdc.remove(key.key());
        PDC.triggerListeners(holder, key.key(), null, null);
        return this;
    }

    @CheckReturnValue
    public boolean has(@NonNull String key) {
        return has(nsk(key));
    }

    @CheckReturnValue
    public boolean has(@NonNull NamespacedKey key) {
        return pdc.has(key);
    }

    @CheckReturnValue
    public boolean has(@NonNull DataKey<?, ?> key) {
        return pdc.has(key.key(), key.type());
    }
}
