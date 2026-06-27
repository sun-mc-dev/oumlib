package dev.oum.oumlib.config;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnusedReturnValue")
public final class ConfigManager<T extends Record & ConfigSection> {

    private final String fileName;
    private final Supplier<T> defaults;
    private final Class<T> type;
    private volatile T current;
    private Consumer<T> reloadCallback;
    private ConfigMigrationRegistry migrationRegistry;

    private ConfigManager(Class<T> type, String fileName, Supplier<T> defaults) {
        this.type = type;
        this.fileName = fileName;
        this.defaults = defaults;
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static <T extends Record & ConfigSection> @NonNull ConfigManager<T> of(
            Class<T> type,
            String fileName,
            Supplier<T> defaults
    ) {
        return new ConfigManager<>(type, fileName, defaults);
    }

    private static Object convertValue(Object val, Class<?> targetType) {
        if (val == null) return null;
        if (targetType.isInstance(val)) return val;

        if (targetType == Component.class) {
            return MiniMessage.miniMessage().deserialize(String.valueOf(val));
        }

        if (targetType == boolean.class || targetType == Boolean.class) {
            if (val instanceof Boolean b) return b;
            if (val instanceof String s) return Boolean.parseBoolean(s);
        }
        if (targetType == int.class || targetType == Integer.class) {
            if (val instanceof Number n) return n.intValue();
            if (val instanceof String s) {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (targetType == double.class || targetType == Double.class) {
            if (val instanceof Number n) return n.doubleValue();
            if (val instanceof String s) {
                try {
                    return Double.parseDouble(s);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (targetType == float.class || targetType == Float.class) {
            if (val instanceof Number n) return n.floatValue();
            if (val instanceof String s) {
                try {
                    return Float.parseFloat(s);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (targetType == long.class || targetType == Long.class) {
            if (val instanceof Number n) return n.longValue();
            if (val instanceof String s) {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (targetType == short.class || targetType == Short.class) {
            if (val instanceof Number n) return n.shortValue();
            if (val instanceof String s) {
                try {
                    return Short.parseShort(s);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (targetType == byte.class || targetType == Byte.class) {
            if (val instanceof Number n) return n.byteValue();
            if (val instanceof String s) {
                try {
                    return Byte.parseByte(s);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return val;
    }

    @SuppressWarnings("unchecked")
    private static Object reconstructValue(Object val, Type targetType, Object defaultVal) {
        if (targetType instanceof ParameterizedType pt) {
            Class<?> rawType = (Class<?>) pt.getRawType();
            if (List.class.isAssignableFrom(rawType)) {
                if (val instanceof List<?> list) {
                    Type elementType = pt.getActualTypeArguments()[0];
                    List<Object> reconstructed = new ArrayList<>();
                    for (Object item : list) {
                        reconstructed.add(reconstructValue(item, elementType, null));
                    }
                    return reconstructed;
                }
            }
            return reconstructValue(val, rawType, defaultVal);
        }

        if (targetType instanceof Class<?> clazz) {
            if (Record.class.isAssignableFrom(clazz) && ConfigSection.class.isAssignableFrom(clazz)) {
                Class<? extends Record> recordType = (Class<? extends Record>) clazz;
                Map<String, Object> valMap;
                if (val instanceof Map) {
                    valMap = (Map<String, Object>) val;
                } else {
                    valMap = new HashMap<>();
                }

                RecordComponent[] comps = recordType.getRecordComponents();
                Object[] args = new Object[comps.length];
                Class<?>[] types = new Class<?>[comps.length];
                for (int i = 0; i < comps.length; i++) {
                    RecordComponent comp = comps[i];
                    String key = toKebab(comp.getName());
                    Object memberVal = valMap.get(key);
                    Object memberDefault = null;
                    if (defaultVal != null) {
                        try {
                            memberDefault = comp.getAccessor().invoke(defaultVal);
                        } catch (Exception ignored) {
                        }
                    }

                    types[i] = comp.getType();
                    Type genericType = comp.getGenericType();
                    Object converted = convertValue(memberVal, types[i]);
                    if (converted == null && types[i] == List.class && memberVal != null) {
                        converted = List.of(memberVal);
                    }

                    args[i] = reconstructValue(converted != null ? converted : memberDefault, genericType, memberDefault);
                }

                try {
                    return recordType.getDeclaredConstructor(types).newInstance(args);
                } catch (Exception e) {
                    OumLib.logError("Failed to reconstruct nested record " + clazz.getSimpleName() + ", using defaults.", e);
                    return defaultVal;
                }
            }
            return val != null ? convertValue(val, clazz) : defaultVal;
        }

        return val;
    }

    @SuppressWarnings("unchecked")
    private static boolean isMissingKey(Map<String, Object> yamlMap, Class<?> targetType) {
        if (yamlMap == null) return true;
        if (Record.class.isAssignableFrom(targetType) && ConfigSection.class.isAssignableFrom(targetType)) {
            for (RecordComponent comp : targetType.getRecordComponents()) {
                String key = toKebab(comp.getName());
                if (!yamlMap.containsKey(key)) {
                    return true;
                }
                Object subVal = yamlMap.get(key);
                if (Record.class.isAssignableFrom(comp.getType()) && ConfigSection.class.isAssignableFrom(comp.getType())) {
                    if (subVal instanceof Map<?, ?> subMap) {
                        if (isMissingKey((Map<String, Object>) subMap, comp.getType())) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String toYamlValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Component comp) {
            String s = MiniMessage.miniMessage().serialize(comp);
            return "\"" + s.replace("\"", "\\\"") + "\"";
        }
        if (value instanceof String s) return "\"" + s.replace("\"", "\\\"") + "\"";
        return String.valueOf(value);
    }

    private static @NonNull String toKebab(@NonNull String camel) {
        return camel
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .toLowerCase();
    }

    public ConfigManager<T> onReload(Consumer<T> callback) {
        this.reloadCallback = callback;
        return this;
    }

    public ConfigManager<T> migrate(@NonNull ConfigMigrationRegistry registry) {
        this.migrationRegistry = registry;
        return this;
    }

    public ConfigManager<T> enableAutoReload() {
        File dir = OumLib.getDataFolder();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ConfigWatcher.watch(dir.toPath(), fileName, this::handleFileChange);
        return this;
    }

    private void handleFileChange() {
        T loaded = load();
        current = loaded;
        if (reloadCallback != null) {
            Scheduler.run(() -> reloadCallback.accept(loaded));
        }
    }

    public T get() {
        if (current == null) current = load();
        return current;
    }

    public void reload() {
        current = load();
        if (reloadCallback != null) reloadCallback.accept(current);
    }

    public void update(T newConfig) {
        this.current = newConfig;
        File file = new File(OumLib.getDataFolder(), fileName);
        save(file, newConfig, new LinkedHashMap<>());
        if (reloadCallback != null) reloadCallback.accept(newConfig);
    }

    private T load() {
        File file = new File(OumLib.getDataFolder(), fileName);
        OumLib.logDebug("Loading config file: " + file.getAbsolutePath() + " (Exists: " + file.exists() + ")");
        T def = defaults.get();

        if (!file.exists()) {
            OumLib.logDebug("Config file does not exist, saving defaults.");
            save(file, def, new LinkedHashMap<>());
            return def;
        }

        Map<String, Object> yaml = YamlParser.parse(file);
        OumLib.logDebug("Parsed YAML map: " + yaml);

        boolean migrated = false;
        if (migrationRegistry != null && !migrationRegistry.migrations().isEmpty()) {
            Object rawVer = yaml.get("config-version");
            int currentVersion = 1;
            if (rawVer instanceof Number num) {
                currentVersion = num.intValue();
            } else if (rawVer instanceof String str) {
                try {
                    currentVersion = Integer.parseInt(str);
                } catch (NumberFormatException ignored) {
                }
            }

            for (Map.Entry<Integer, Consumer<Map<String, Object>>> entry : migrationRegistry.migrations().entrySet()) {
                int targetVersion = entry.getKey();
                if (targetVersion > currentVersion) {
                    OumLib.logDebug("Applying migration to version " + targetVersion + " for " + fileName);
                    entry.getValue().accept(yaml);
                    currentVersion = targetVersion;
                    migrated = true;
                }
            }

            if (migrated) {
                yaml.put("config-version", currentVersion);
            }
        }

        // Collect unknown keys the user may have added — preserved on save.
        Map<String, Object> unknownKeys = new LinkedHashMap<>();
        for (String yamlKey : yaml.keySet()) {
            boolean isKnown = false;
            for (RecordComponent comp : type.getRecordComponents()) {
                if (toKebab(comp.getName()).equals(yamlKey)) {
                    isKnown = true;
                    break;
                }
            }
            if (!isKnown) unknownKeys.put(yamlKey, yaml.get(yamlKey));
        }

        T loaded = type.cast(reconstructValue(yaml, type, def));
        OumLib.logDebug("Reconstructed config object: " + loaded);

        boolean needsSave = migrated || isMissingKey(yaml, type);
        if (needsSave) {
            OumLib.logDebug("Config missing keys or migrated, saving updated config.");
            save(file, loaded, unknownKeys);
        }

        return loaded;
    }

    private void save(@NonNull File file, T config, @NonNull Map<String, Object> preservedUnknown) {
        File parent = file.getParentFile();
        if (parent != null) {
            boolean ignored = parent.mkdirs();
        }
        StringBuilder yaml = new StringBuilder();

        Comment header = type.getAnnotation(Comment.class);
        if (header != null) {
            for (String line : header.value()) {
                yaml.append("# ").append(line).append('\n');
            }
            yaml.append('\n');
        }

        writeRecord(yaml, config, 0);

        if (!preservedUnknown.isEmpty()) {
            yaml.append("\n# Additional keys\n");
            writeMap(yaml, preservedUnknown, 0);
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(yaml.toString());
        } catch (IOException e) {
            OumLib.logError("Failed to save " + fileName + ".", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeRecordAsListItem(StringBuilder yaml, @NonNull Record config, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        boolean first = true;
        for (RecordComponent comp : config.getClass().getRecordComponents()) {
            try {
                Object value = comp.getAccessor().invoke(config);
                String key = toKebab(comp.getName());
                if (first) {
                    yaml.append(indent).append("- ").append(key).append(": ");
                    first = false;
                } else {
                    yaml.append(indent).append("  ").append(key).append(": ");
                }

                switch (value) {
                    case Record subRecord when subRecord instanceof ConfigSection -> {
                        yaml.append('\n');
                        writeRecord(yaml, subRecord, indentLevel + 2);
                    }
                    case Map<?, ?> map -> {
                        yaml.append('\n');
                        writeMap(yaml, (Map<String, Object>) map, indentLevel + 2);
                    }
                    case Iterable<?> iter -> {
                        yaml.append('\n');
                        String subIndent = "  ".repeat(indentLevel + 2);
                        for (Object item : iter) {
                            if (item instanceof Record subRec && subRec instanceof ConfigSection) {
                                writeRecordAsListItem(yaml, subRec, indentLevel + 2);
                            } else {
                                yaml.append(subIndent).append("- ").append(toYamlValue(item)).append('\n');
                            }
                        }
                    }
                    case null, default -> yaml.append(toYamlValue(value)).append('\n');
                }
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeRecord(StringBuilder yaml, @NonNull Record config, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        for (RecordComponent comp : config.getClass().getRecordComponents()) {
            Comment comment = comp.getAnnotation(Comment.class);
            if (comment != null) {
                for (String line : comment.value()) {
                    yaml.append(indent).append("# ").append(line).append('\n');
                }
            }
            try {
                Object value = comp.getAccessor().invoke(config);
                String key = toKebab(comp.getName());
                switch (value) {
                    case Record subRecord when subRecord instanceof ConfigSection -> {
                        yaml.append(indent).append(key).append(":\n");
                        writeRecord(yaml, subRecord, indentLevel + 1);
                    }
                    case Map<?, ?> map -> {
                        yaml.append(indent).append(key).append(":\n");
                        writeMap(yaml, (Map<String, Object>) map, indentLevel + 1);
                    }
                    case Iterable<?> iter -> {
                        yaml.append(indent).append(key).append(":\n");
                        for (Object item : iter) {
                            if (item instanceof Record subRec && subRec instanceof ConfigSection) {
                                writeRecordAsListItem(yaml, subRec, indentLevel + 1);
                            } else {
                                yaml.append(indent).append("  - ").append(toYamlValue(item)).append('\n');
                            }
                        }
                    }
                    case null, default ->
                            yaml.append(indent).append(key).append(": ").append(toYamlValue(value)).append('\n');
                }
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeMap(StringBuilder yaml, @NonNull Map<String, Object> map, int indentLevel) {
        String indent = "  ".repeat(indentLevel);
        map.forEach((k, v) -> {
            switch (v) {
                case Record subRecord when subRecord instanceof ConfigSection -> {
                    yaml.append(indent).append(k).append(":\n");
                    writeRecord(yaml, subRecord, indentLevel + 1);
                }
                case Map<?, ?> subMap -> {
                    yaml.append(indent).append(k).append(":\n");
                    writeMap(yaml, (Map<String, Object>) subMap, indentLevel + 1);
                }
                case Iterable<?> iter -> {
                    yaml.append(indent).append(k).append(":\n");
                    for (Object item : iter) {
                        if (item instanceof Record subRec && subRec instanceof ConfigSection) {
                            writeRecordAsListItem(yaml, subRec, indentLevel + 1);
                        } else {
                            yaml.append(indent).append("  - ").append(toYamlValue(item)).append('\n');
                        }
                    }
                }
                case null, default -> yaml.append(indent).append(k).append(": ").append(toYamlValue(v)).append('\n');
            }
        });
    }
}