package dev.oum.oumlib.bridge;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class StatisticsBridge {

    private StatisticsBridge() {
    }

    public static @NonNull Map<String, Integer> capture(@NonNull Player player, @NonNull List<String> tracked) {
        Map<String, Integer> map = new HashMap<>();
        for (String entry : tracked) {
            try {
                String[] parts = entry.split(":", 2);
                Statistic statistic = Statistic.valueOf(parts[0].toUpperCase(Locale.ROOT));
                if (parts.length == 1) {
                    map.put(entry, player.getStatistic(statistic));
                } else {
                    String param = parts[1].toUpperCase(Locale.ROOT);
                    if (statistic.getType() == Statistic.Type.BLOCK || statistic.getType() == Statistic.Type.ITEM) {
                        Material mat = Material.valueOf(param);
                        map.put(entry, player.getStatistic(statistic, mat));
                    } else if (statistic.getType() == Statistic.Type.ENTITY) {
                        EntityType entityType = EntityType.valueOf(param);
                        map.put(entry, player.getStatistic(statistic, entityType));
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return map;
    }

    public static void apply(@NonNull Player player, @NonNull List<String> tracked, @NonNull Map<String, Integer> values) {
        for (String entry : tracked) {
            try {
                String[] parts = entry.split(":", 2);
                Statistic statistic = Statistic.valueOf(parts[0].toUpperCase(Locale.ROOT));
                int value = values.getOrDefault(entry, 0);
                if (parts.length == 1) {
                    player.setStatistic(statistic, value);
                } else {
                    String param = parts[1].toUpperCase(Locale.ROOT);
                    if (statistic.getType() == Statistic.Type.BLOCK || statistic.getType() == Statistic.Type.ITEM) {
                        Material mat = Material.valueOf(param);
                        player.setStatistic(statistic, mat, value);
                    } else if (statistic.getType() == Statistic.Type.ENTITY) {
                        EntityType entityType = EntityType.valueOf(param);
                        player.setStatistic(statistic, entityType, value);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
