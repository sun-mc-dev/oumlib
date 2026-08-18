package dev.oum.oumlib.text;

import dev.oum.oumlib.config.ConfigSection;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.RecordComponent;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class Placeholders {

    private static final Pattern PLACEHOLDER = Pattern.compile("%(.*?)%");
    private static final Map<String, PlaceholderSupplier> registered = new ConcurrentHashMap<>();

    private Placeholders() {
    }

    public static void register(@NonNull String key, @NonNull PlaceholderSupplier supplier) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(supplier, "supplier");
        registered.put(key, supplier);
    }

    public static <T extends Record & ConfigSection> void register(@NonNull String key, @NonNull ConfigPlaceholderSupplier<T> supplier, T config) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(supplier, "supplier");
        Objects.requireNonNull(config, "config");
        registered.put(key, player -> supplier.get(player, config));
    }

    public static String parse(@NonNull Object player, @NonNull String text) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(text, "text");
        return replaceGeneral(player, text);
    }

    public static <T extends Record & ConfigSection> String parse(@NonNull Object player, @NonNull String text, T config) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(text, "text");

        if (config != null) {
            text = replaceConfigPlaceholders(text, config);
        }

        return replaceGeneral(player, text);
    }

    private static String replaceGeneral(@NonNull Object player, @NonNull String text) {
        return PLACEHOLDER.matcher(text).replaceAll(match -> {
            String key = match.group(1);
            PlaceholderSupplier supplier = registered.get(key);
            if (supplier != null) {
                String value = supplier.get(player);
                return value != null ? value : "";
            }
            return match.group(0);
        });
    }

    private static <T extends Record> String replaceConfigPlaceholders(String text, T config) {
        String result = text;
        try {
            for (RecordComponent comp : config.getClass().getRecordComponents()) {
                Object val = comp.getAccessor().invoke(config);
                String valStr = val != null ? String.valueOf(val) : "";
                result = result.replace("%config_" + comp.getName() + "%", valStr);
                result = result.replace("%config_" + toKebab(comp.getName()) + "%", valStr);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private static @NonNull String toKebab(@NonNull String camel) {
        return camel
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .toLowerCase();
    }

    @FunctionalInterface
    public interface PlaceholderSupplier {
        String get(Object player);
    }

    @FunctionalInterface
    public interface ConfigPlaceholderSupplier<T> {
        String get(Object player, T config);
    }
}