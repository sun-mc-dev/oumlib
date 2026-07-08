package dev.oum.oumlib.util;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class PotionSerializer {

    private PotionSerializer() {
    }

    public static @NonNull String serialize(@NonNull Collection<@NonNull PotionEffect> effects) {
        if (effects.isEmpty()) {
            return "";
        }
        List<String> list = new ArrayList<>();
        for (PotionEffect e : effects) {
            list.add(e.getType().getKey().toString() + "," +
                    e.getDuration() + "," +
                    e.getAmplifier() + "," +
                    (e.isAmbient() ? 1 : 0) + "," +
                    (e.hasParticles() ? 1 : 0) + "," +
                    (e.hasIcon() ? 1 : 0));
        }
        return String.join(";", list);
    }

    public static @NonNull Collection<@NonNull PotionEffect> deserialize(@NonNull String str) {
        List<PotionEffect> effects = new ArrayList<>();
        if (str.isEmpty()) {
            return effects;
        }
        String[] entries = str.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(",");
            if (parts.length < 3) {
                continue;
            }
            String typeStr = parts[0];
            NamespacedKey key = typeStr.contains(":")
                    ? NamespacedKey.fromString(typeStr)
                    : NamespacedKey.minecraft(typeStr.toLowerCase(Locale.ROOT));
            PotionEffectType type = key != null ? Registry.MOB_EFFECT.get(key) : null;
            if (type == null) {
                continue;
            }
            try {
                int duration = Integer.parseInt(parts[1]);
                int amplifier = Integer.parseInt(parts[2]);
                boolean ambient = parts.length > 3 && parts[3].equals("1");
                boolean particles = parts.length > 4 && parts[4].equals("1");
                boolean icon = parts.length > 5 && parts[5].equals("1");
                effects.add(new PotionEffect(type, duration, amplifier, ambient, particles, icon));
            } catch (NumberFormatException ignored) {
            }
        }
        return effects;
    }
}
