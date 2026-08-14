package dev.oum.oumlib.bridge.combat;

import dev.oum.oumlib.pdc.metadata.VolatileData;
import dev.oum.oumlib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.time.Duration;

public final class CombatBridge {

    private static boolean combatLogXChecked = false;
    private static boolean combatLogXAvailable = false;
    private static boolean pvpManagerChecked = false;
    private static boolean pvpManagerAvailable = false;
    private static boolean deluxeCombatChecked = false;
    private static boolean deluxeCombatAvailable = false;

    private CombatBridge() {
    }

    public static boolean isAvailable() {
        return hasCombatLogX() || hasPvPManager() || hasDeluxeCombat();
    }

    public static boolean isInCombat(@NonNull Player player) {
        if (hasCombatLogX()) {
            try {
                if (checkCombatLogX(player)) return true;
            } catch (Throwable ignored) {
            }
        }

        if (hasPvPManager()) {
            try {
                if (checkPvPManager(player)) return true;
            } catch (Throwable ignored) {
            }
        }

        if (hasDeluxeCombat()) {
            try {
                if (checkDeluxeCombat(player)) return true;
            } catch (Throwable ignored) {
            }
        }

        return VolatileData.getOrDefault(player, "bounty_combat", false);
    }

    public static void tag(@NonNull Player player, @NonNull Duration duration) {
        VolatileData.set(player, "bounty_combat", true);
        Scheduler.runLater(duration.toMillis() / 50L, () -> {
            if (player.isOnline()) {
                VolatileData.set(player, "bounty_combat", false);
            }
        });

        if (hasCombatLogX()) {
            try {
                tagCombatLogX(player, duration);
            } catch (Throwable ignored) {
            }
        }

        if (hasDeluxeCombat()) {
            try {
                tagDeluxeCombat(player, (int) duration.toSeconds());
            } catch (Throwable ignored) {
            }
        }
    }

    public static void untag(@NonNull Player player) {
        VolatileData.set(player, "bounty_combat", false);
    }

    public static boolean hasCombatLogX() {
        if (!combatLogXChecked) {
            combatLogXChecked = true;
            combatLogXAvailable = Bukkit.getPluginManager().getPlugin("CombatLogX") != null;
        }
        return combatLogXAvailable;
    }

    public static boolean hasPvPManager() {
        if (!pvpManagerChecked) {
            pvpManagerChecked = true;
            pvpManagerAvailable = Bukkit.getPluginManager().getPlugin("PvPManager") != null;
        }
        return pvpManagerAvailable;
    }

    public static boolean hasDeluxeCombat() {
        if (!deluxeCombatChecked) {
            deluxeCombatChecked = true;
            deluxeCombatAvailable = Bukkit.getPluginManager().getPlugin("DeluxeCombat") != null;
        }
        return deluxeCombatAvailable;
    }

    private static boolean checkCombatLogX(@NonNull Player player) throws Exception {
        Class<?> clxClass = Class.forName("com.sirblobman.combatlogx.api.ICombatLogX");
        Object plugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (plugin == null) return false;
        Object manager = clxClass.getMethod("getCombatManager").invoke(plugin);
        Method isInCombat = manager.getClass().getMethod("isInCombat", Player.class);
        return (boolean) isInCombat.invoke(manager, player);
    }

    private static void tagCombatLogX(@NonNull Player player, @NonNull Duration duration) throws Exception {
        Class<?> clxClass = Class.forName("com.sirblobman.combatlogx.api.ICombatLogX");
        Object plugin = Bukkit.getPluginManager().getPlugin("CombatLogX");
        if (plugin == null) return;
        Object manager = clxClass.getMethod("getCombatManager").invoke(plugin);
        Method tagMethod = manager.getClass().getMethod("tag", Player.class, Player.class, int.class);
        tagMethod.invoke(manager, player, null, (int) duration.toSeconds());
    }

    private static boolean checkPvPManager(@NonNull Player player) throws Exception {
        Class<?> pmClass;
        try {
            pmClass = Class.forName("me.NoChance.PvPManager.PvPManager");
        } catch (ClassNotFoundException e) {
            pmClass = Class.forName("me.noonspill.pvpmanager.PvPManager");
        }
        Method getInstance = pmClass.getMethod("getInstance");
        Object instance = getInstance.invoke(null);
        Object playerHandler = instance.getClass().getMethod("getPlayerHandler").invoke(instance);
        Object pvPlayer = playerHandler.getClass().getMethod("get", Player.class).invoke(playerHandler, player);
        if (pvPlayer == null) return false;
        Method isInCombat = pvPlayer.getClass().getMethod("isInCombat");
        return (boolean) isInCombat.invoke(pvPlayer);
    }

    private static boolean checkDeluxeCombat(@NonNull Player player) throws Exception {
        try {
            Class<?> dcApiClass = Class.forName("nl.marido.deluxecombat.api.DeluxeCombatAPI");
            Object apiInstance = dcApiClass.getDeclaredConstructor().newInstance();
            Method inCombat = dcApiClass.getMethod("isInCombat", Player.class);
            return (boolean) inCombat.invoke(apiInstance, player);
        } catch (Throwable t) {
            Class<?> dcClass = Class.forName("nl.marido.deluxecombat.DeluxeCombat");
            Object instance = dcClass.getMethod("getInstance").invoke(null);
            Object combatHandler = instance.getClass().getMethod("getCombatHandler").invoke(instance);
            Method isTagged = combatHandler.getClass().getMethod("isTagged", Player.class);
            return (boolean) isTagged.invoke(combatHandler, player);
        }
    }

    private static void tagDeluxeCombat(@NonNull Player player, int seconds) throws Exception {
        try {
            Class<?> dcApiClass = Class.forName("nl.marido.deluxecombat.api.DeluxeCombatAPI");
            Object apiInstance = dcApiClass.getDeclaredConstructor().newInstance();
            Method tagMethod = dcApiClass.getMethod("tag", Player.class, int.class);
            tagMethod.invoke(apiInstance, player, seconds);
        } catch (Throwable ignored) {
        }
    }
}
