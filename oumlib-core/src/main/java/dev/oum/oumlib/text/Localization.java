package dev.oum.oumlib.text;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.config.YamlParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class Localization {

    private static final Map<String, Map<String, String>> translations = new HashMap<>();
    private static String defaultLang = "en";

    private Localization() {
    }

    public static void load(@NonNull String defaultLanguageCode) {
        defaultLang = defaultLanguageCode.toLowerCase();
        translations.clear();

        File dataFolder = OumLib.getDataFolder();
        File langFolder = new File(dataFolder, "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        List<String> bundledLangs = List.of("en", "es", "ko", "de", "fr", "ja", "it");
        for (String lang : bundledLangs) {
            String filePath = "lang/" + lang + ".yml";
            File targetFile = new File(dataFolder, filePath);
            if (!targetFile.exists()) {
                InputStream rawIn = null;
                if (OumLib.isPaper() && OumLib.plugin() != null) {
                    rawIn = OumLib.plugin().getResource(filePath);
                }
                if (rawIn == null) {
                    rawIn = Localization.class.getClassLoader().getResourceAsStream(filePath);
                }
                if (rawIn != null) {
                    try (InputStream in = rawIn) {
                        Files.copy(in, targetFile.toPath());
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        File[] files = langFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String langCode = name.substring(0, name.lastIndexOf('.')).toLowerCase();
                try {
                    Map<String, Object> parsed = YamlParser.parse(file);
                    Map<String, String> langMap = new HashMap<>();
                    flatten("", parsed, langMap);
                    translations.put(langCode, langMap);
                } catch (Exception e) {
                    OumLib.logError("Failed to load localization file: " + file.getName(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void flatten(@NonNull String prefix, @NonNull Map<String, Object> map, @NonNull Map<String, String> target) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> subMap) {
                flatten(key, (Map<String, Object>) subMap, target);
            } else if (value instanceof List<?> list) {
                target.put(key, String.valueOf(value));
                for (int i = 0; i < list.size(); i++) {
                    target.put(key + "." + i, String.valueOf(list.get(i)));
                }
            } else if (value != null) {
                target.put(key, String.valueOf(value));
            }
        }
    }

    public static @NonNull Component translate(@NonNull String key, TagResolver... resolvers) {
        return translateFor(defaultLang, key, resolvers);
    }

    public static @NonNull Component translateFor(@NonNull String lang, @NonNull String key, TagResolver... resolvers) {
        String message = getRaw(lang, key);
        if (message == null) {
            return Component.text(key);
        }
        return MiniMessage.miniMessage().deserialize(message, resolvers);
    }

    public static @NonNull Component translateFor(@Nullable Object playerObj, @NonNull String key, TagResolver... resolvers) {
        String locale = resolveLocale(playerObj);
        return translateFor(locale, key, resolvers);
    }

    public static @NonNull List<Component> translateList(@NonNull String key, TagResolver... resolvers) {
        return translateListFor(defaultLang, key, resolvers);
    }

    public static @NonNull List<Component> translateListFor(@Nullable Object playerObj, @NonNull String key, TagResolver... resolvers) {
        String locale = resolveLocale(playerObj);
        List<Component> result = new ArrayList<>();
        int index = 0;
        while (true) {
            String raw = getRaw(locale, key + "." + index);
            if (raw == null) break;
            result.add(MiniMessage.miniMessage().deserialize(raw, resolvers));
            index++;
        }
        return result;
    }

    public static @Nullable String getRaw(@NonNull String key) {
        return getRaw(defaultLang, key);
    }

    public static @Nullable String getRaw(@Nullable Object playerObj, @NonNull String key) {
        if (playerObj instanceof String langStr) {
            return getRaw(langStr, key);
        }
        String locale = resolveLocale(playerObj);
        return getRaw(locale, key);
    }

    public static @NonNull String getRawOrDefault(@Nullable Object playerObj, @NonNull String key, @NonNull String fallback) {
        String val = getRaw(playerObj, key);
        return val != null ? val : fallback;
    }

    public static @NonNull String getRawOrDefault(@NonNull String key, @NonNull String fallback) {
        String val = getRaw(defaultLang, key);
        return val != null ? val : fallback;
    }

    public static @Nullable String getRaw(@NonNull String lang, @NonNull String key) {
        String langCode = lang.toLowerCase();
        Map<String, String> map = translations.get(langCode);
        if (map != null && map.containsKey(key)) {
            return map.get(key);
        }
        Map<String, String> defaultMap = translations.get(defaultLang);
        if (defaultMap != null && defaultMap.containsKey(key)) {
            return defaultMap.get(key);
        }
        Map<String, String> enMap = translations.get("en");
        if (enMap != null && enMap.containsKey(key)) {
            return enMap.get(key);
        }
        return null;
    }

    private static @NonNull String resolveLocale(@Nullable Object playerObj) {
        return defaultLang;
    }
}
