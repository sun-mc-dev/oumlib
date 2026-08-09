package dev.oum.oumlib.pdc;

import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DataKey<P, C>(
        @NonNull NamespacedKey key,
        @NonNull PersistentDataType<P, C> type
) {

    public static final PersistentDataType<Byte, Boolean> BOOLEAN_TYPE = new PersistentDataType<>() {
        @Override
        public @NonNull Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @Override
        public @NonNull Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @Override
        public @NonNull Byte toPrimitive(@NonNull Boolean complex, @NonNull PersistentDataAdapterContext context) {
            return (byte) (complex ? 1 : 0);
        }

        @Override
        public @NonNull Boolean fromPrimitive(@NonNull Byte primitive, @NonNull PersistentDataAdapterContext context) {
            return primitive == 1;
        }
    };
    public static final PersistentDataType<byte[], UUID> UUID_TYPE = new PersistentDataType<>() {
        @Override
        public @NonNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NonNull Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public byte @NonNull [] toPrimitive(@NonNull UUID complex, @NonNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(complex.getMostSignificantBits());
            bb.putLong(complex.getLeastSignificantBits());
            return bb.array();
        }

        @Override
        public @NonNull UUID fromPrimitive(byte @NonNull [] primitive, @NonNull PersistentDataAdapterContext context) {
            ByteBuffer bb = ByteBuffer.wrap(primitive);
            return new UUID(bb.getLong(), bb.getLong());
        }
    };
    public static final PersistentDataType<Long, Instant> INSTANT_TYPE = new PersistentDataType<>() {
        @Override
        public @NonNull Class<Long> getPrimitiveType() {
            return Long.class;
        }

        @Override
        public @NonNull Class<Instant> getComplexType() {
            return Instant.class;
        }

        @Override
        public @NonNull Long toPrimitive(@NonNull Instant complex, @NonNull PersistentDataAdapterContext context) {
            return complex.toEpochMilli();
        }

        @Override
        public @NonNull Instant fromPrimitive(@NonNull Long primitive, @NonNull PersistentDataAdapterContext context) {
            return Instant.ofEpochMilli(primitive);
        }
    };
    public static final PersistentDataType<String, Location> LOCATION_TYPE = new PersistentDataType<>() {
        @Override
        public @NonNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NonNull Class<Location> getComplexType() {
            return Location.class;
        }

        @Override
        public @NonNull String toPrimitive(@NonNull Location complex, @NonNull PersistentDataAdapterContext context) {
            String world = complex.getWorld() != null ? complex.getWorld().getName() : "world";
            return world + ";" + complex.getX() + ";" + complex.getY() + ";" + complex.getZ() + ";" + complex.getYaw() + ";" + complex.getPitch();
        }

        @Override
        public @NonNull Location fromPrimitive(@NonNull String primitive, @NonNull PersistentDataAdapterContext context) {
            String[] parts = primitive.split(";");
            World w = Bukkit.getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;
            return new Location(w, x, y, z, yaw, pitch);
        }
    };
    private static final Gson GSON = new Gson();
    public static final PersistentDataType<String, List<String>> STRING_LIST_TYPE = new PersistentDataType<>() {
        @Override
        public @NonNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NonNull Class<List<String>> getComplexType() {
            return (Class<List<String>>) (Class<?>) List.class;
        }

        @Override
        public @NonNull String toPrimitive(@NonNull List<String> complex, @NonNull PersistentDataAdapterContext context) {
            return GSON.toJson(complex);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NonNull List<String> fromPrimitive(@NonNull String primitive, @NonNull PersistentDataAdapterContext context) {
            return GSON.fromJson(primitive, List.class);
        }
    };

    public static @NonNull DataKey<String, String> string(@NonNull NamespacedKey key) {
        return new DataKey<>(key, PersistentDataType.STRING);
    }

    public static @NonNull DataKey<String, String> string(@NonNull String key) {
        return string(createKey(key));
    }

    public static @NonNull DataKey<Integer, Integer> integer(@NonNull NamespacedKey key) {
        return new DataKey<>(key, PersistentDataType.INTEGER);
    }

    public static @NonNull DataKey<Integer, Integer> integer(@NonNull String key) {
        return integer(createKey(key));
    }

    public static @NonNull DataKey<Double, Double> doubles(@NonNull NamespacedKey key) {
        return new DataKey<>(key, PersistentDataType.DOUBLE);
    }

    public static @NonNull DataKey<Double, Double> doubles(@NonNull String key) {
        return doubles(createKey(key));
    }

    public static @NonNull DataKey<Float, Float> floats(@NonNull NamespacedKey key) {
        return new DataKey<>(key, PersistentDataType.FLOAT);
    }

    public static @NonNull DataKey<Float, Float> floats(@NonNull String key) {
        return floats(createKey(key));
    }

    public static @NonNull DataKey<Long, Long> longs(@NonNull NamespacedKey key) {
        return new DataKey<>(key, PersistentDataType.LONG);
    }

    public static @NonNull DataKey<Long, Long> longs(@NonNull String key) {
        return longs(createKey(key));
    }

    public static @NonNull DataKey<Byte, Byte> bytes(@NonNull NamespacedKey key) {
        return new DataKey<>(key, PersistentDataType.BYTE);
    }

    public static @NonNull DataKey<Byte, Byte> bytes(@NonNull String key) {
        return bytes(createKey(key));
    }

    public static @NonNull DataKey<Byte, Boolean> bool(@NonNull NamespacedKey key) {
        return new DataKey<>(key, BOOLEAN_TYPE);
    }

    public static @NonNull DataKey<Byte, Boolean> bool(@NonNull String key) {
        return bool(createKey(key));
    }

    public static @NonNull DataKey<byte[], UUID> uuid(@NonNull NamespacedKey key) {
        return new DataKey<>(key, UUID_TYPE);
    }

    public static @NonNull DataKey<byte[], UUID> uuid(@NonNull String key) {
        return uuid(createKey(key));
    }

    public static @NonNull DataKey<Long, Instant> instant(@NonNull NamespacedKey key) {
        return new DataKey<>(key, INSTANT_TYPE);
    }

    public static @NonNull DataKey<Long, Instant> instant(@NonNull String key) {
        return instant(createKey(key));
    }

    public static @NonNull DataKey<String, Location> location(@NonNull NamespacedKey key) {
        return new DataKey<>(key, LOCATION_TYPE);
    }

    public static @NonNull DataKey<String, Location> location(@NonNull String key) {
        return location(createKey(key));
    }

    public static @NonNull DataKey<String, List<String>> stringList(@NonNull NamespacedKey key) {
        return new DataKey<>(key, STRING_LIST_TYPE);
    }

    public static @NonNull DataKey<String, List<String>> stringList(@NonNull String key) {
        return stringList(createKey(key));
    }

    public static <T> @NonNull DataKey<String, T> json(@NonNull NamespacedKey key, @NonNull Class<T> clazz) {
        return new DataKey<>(key, new JsonDataType<>(clazz));
    }

    public static <T> @NonNull DataKey<String, T> json(@NonNull String key, @NonNull Class<T> clazz) {
        return json(createKey(key), clazz);
    }

    public static <P, C> @NonNull DataKey<P, C> custom(@NonNull NamespacedKey key, @NonNull PersistentDataType<P, C> type) {
        return new DataKey<>(key, type);
    }

    public static <P, C> @NonNull DataKey<P, C> custom(@NonNull String key, @NonNull PersistentDataType<P, C> type) {
        return custom(createKey(key), type);
    }

    private static NamespacedKey createKey(@NonNull String key) {
        if (key.contains(":")) {
            return NamespacedKey.fromString(key);
        }
        return new NamespacedKey(OumLib.plugin(), key);
    }

    private record JsonDataType<T>(Class<T> clazz) implements PersistentDataType<String, T> {

        @Override
            public @NonNull Class<String> getPrimitiveType() {
                return String.class;
            }

            @Override
            public @NonNull Class<T> getComplexType() {
                return clazz;
            }

            @Override
            public @NonNull String toPrimitive(@NonNull T complex, @NonNull PersistentDataAdapterContext context) {
                return GSON.toJson(complex);
            }

            @Override
            public @NonNull T fromPrimitive(@NonNull String primitive, @NonNull PersistentDataAdapterContext context) {
                return GSON.fromJson(primitive, clazz);
            }
        }
}
