package dev.oum.oumlib.pdc;

import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.inventory.ItemSerializer;
import dev.oum.oumlib.math.Locations;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;

public final class PdcModel {

    private static final Gson GSON = new Gson();

    private PdcModel() {
    }

    public static <T extends Record> void write(@NonNull PersistentDataContainer container, @NonNull T recordInstance) {
        Objects.requireNonNull(container);
        Objects.requireNonNull(recordInstance);
        Class<?> recordClass = recordInstance.getClass();
        RecordComponent[] components = recordClass.getRecordComponents();
        if (components == null) return;

        for (RecordComponent comp : components) {
            String name = toKebab(comp.getName());
            NamespacedKey key = new NamespacedKey(OumLib.plugin(), name);
            try {
                Object value = comp.getAccessor().invoke(recordInstance);
                if (value == null) {
                    container.remove(key);
                } else {
                    writeField(container, key, comp.getType(), value);
                }
            } catch (Exception e) {
                OumLib.logError("Failed to write record field " + comp.getName() + " to PDC", e);
            }
        }
    }

    @CheckReturnValue
    public static <T extends Record> @NonNull Optional<T> read(@NonNull PersistentDataContainer container, @NonNull Class<T> recordClass) {
        Objects.requireNonNull(container);
        Objects.requireNonNull(recordClass);
        RecordComponent[] components = recordClass.getRecordComponents();
        if (components == null) return Optional.empty();

        Class<?>[] paramTypes = new Class<?>[components.length];
        Object[] paramValues = new Object[components.length];
        boolean hasAny = false;

        for (int i = 0; i < components.length; i++) {
            RecordComponent comp = components[i];
            paramTypes[i] = comp.getType();
            String name = toKebab(comp.getName());
            NamespacedKey key = new NamespacedKey(OumLib.plugin(), name);
            try {
                Object value = readField(container, key, comp.getType());
                paramValues[i] = value != null ? value : defaultValue(comp.getType());
                if (value != null) {
                    hasAny = true;
                }
            } catch (Exception e) {
                paramValues[i] = defaultValue(comp.getType());
            }
        }

        if (!hasAny) {
            return Optional.empty();
        }

        try {
            Constructor<T> constructor = recordClass.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return Optional.of(constructor.newInstance(paramValues));
        } catch (Exception e) {
            OumLib.logError("Failed to construct record " + recordClass.getName() + " from PDC", e);
            return Optional.empty();
        }
    }

    private static void writeField(@NonNull PersistentDataContainer container, @NonNull NamespacedKey key,
                                   @NonNull Class<?> type, @NonNull Object value) {
        if (type == String.class) {
            container.set(key, PersistentDataType.STRING, (String) value);
        } else if (type == int.class || type == Integer.class) {
            container.set(key, PersistentDataType.INTEGER, (Integer) value);
        } else if (type == double.class || type == Double.class) {
            container.set(key, PersistentDataType.DOUBLE, (Double) value);
        } else if (type == float.class || type == Float.class) {
            container.set(key, PersistentDataType.FLOAT, (Float) value);
        } else if (type == long.class || type == Long.class) {
            container.set(key, PersistentDataType.LONG, (Long) value);
        } else if (type == byte.class || type == Byte.class) {
            container.set(key, PersistentDataType.BYTE, (Byte) value);
        } else if (type == short.class || type == Short.class) {
            container.set(key, PersistentDataType.SHORT, (Short) value);
        } else if (type == boolean.class || type == Boolean.class) {
            container.set(key, PersistentDataType.BYTE, (byte) ((Boolean) value ? 1 : 0));
        } else if (type == UUID.class) {
            UUID uuid = (UUID) value;
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
            container.set(key, PersistentDataType.BYTE_ARRAY, bb.array());
        } else if (type == Instant.class) {
            container.set(key, PersistentDataType.LONG, ((Instant) value).toEpochMilli());
        } else if (type == Location.class) {
            container.set(key, PersistentDataType.STRING, Locations.serialize((Location) value));
        } else if (type == ItemStack.class) {
            container.set(key, PersistentDataType.STRING, ItemSerializer.serialize((ItemStack) value));
        } else if (Record.class.isAssignableFrom(type)) {
            PersistentDataContainer nested = container.getAdapterContext().newPersistentDataContainer();
            @SuppressWarnings("unchecked")
            Class<? extends Record> recordType = (Class<? extends Record>) type;
            writeNested(nested, (Record) value);
            container.set(key, PersistentDataType.TAG_CONTAINER, nested);
        } else if (List.class.isAssignableFrom(type)) {
            container.set(key, PersistentDataType.STRING, GSON.toJson(value));
        } else {
            container.set(key, PersistentDataType.STRING, GSON.toJson(value));
        }
    }

    private static void writeNested(@NonNull PersistentDataContainer container, @NonNull Record record) {
        write(container, record);
    }

    private static @Nullable Object readField(@NonNull PersistentDataContainer container, @NonNull NamespacedKey key,
                                              @NonNull Class<?> type) {
        if (!container.has(key)) return null;

        if (type == String.class) {
            return container.get(key, PersistentDataType.STRING);
        } else if (type == int.class || type == Integer.class) {
            return container.get(key, PersistentDataType.INTEGER);
        } else if (type == double.class || type == Double.class) {
            return container.get(key, PersistentDataType.DOUBLE);
        } else if (type == float.class || type == Float.class) {
            return container.get(key, PersistentDataType.FLOAT);
        } else if (type == long.class || type == Long.class) {
            return container.get(key, PersistentDataType.LONG);
        } else if (type == byte.class || type == Byte.class) {
            return container.get(key, PersistentDataType.BYTE);
        } else if (type == short.class || type == Short.class) {
            return container.get(key, PersistentDataType.SHORT);
        } else if (type == boolean.class || type == Boolean.class) {
            Byte b = container.get(key, PersistentDataType.BYTE);
            return b != null && b != 0;
        } else if (type == UUID.class) {
            byte[] bytes = container.get(key, PersistentDataType.BYTE_ARRAY);
            if (bytes == null || bytes.length < 16) return null;
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            return new UUID(bb.getLong(), bb.getLong());
        } else if (type == Instant.class) {
            Long epoch = container.get(key, PersistentDataType.LONG);
            return epoch != null ? Instant.ofEpochMilli(epoch) : null;
        } else if (type == Location.class) {
            String raw = container.get(key, PersistentDataType.STRING);
            return raw != null ? Locations.deserialize(raw) : null;
        } else if (type == ItemStack.class) {
            String raw = container.get(key, PersistentDataType.STRING);
            return raw != null ? ItemSerializer.deserialize(raw) : null;
        } else if (Record.class.isAssignableFrom(type)) {
            PersistentDataContainer nested = container.get(key, PersistentDataType.TAG_CONTAINER);
            if (nested == null) return null;
            @SuppressWarnings("unchecked")
            Class<? extends Record> recordType = (Class<? extends Record>) type;
            return read(nested, recordType).orElse(null);
        } else if (List.class.isAssignableFrom(type)) {
            String raw = container.get(key, PersistentDataType.STRING);
            if (raw == null) return null;
            return Arrays.asList(GSON.fromJson(raw, String[].class));
        } else {
            String raw = container.get(key, PersistentDataType.STRING);
            if (raw == null) return null;
            return GSON.fromJson(raw, type);
        }
    }

    private static @Nullable Object defaultValue(@NonNull Class<?> type) {
        if (type == int.class) return 0;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == long.class) return 0L;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == boolean.class) return false;
        if (type == List.class) return List.of();
        return null;
    }

    private static @NonNull String toKebab(@NonNull String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('-');
                sb.append(Character.toLowerCase(c));
            } else if (c == '_') {
                sb.append('-');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
