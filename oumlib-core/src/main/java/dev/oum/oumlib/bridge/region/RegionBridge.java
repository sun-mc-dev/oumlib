package dev.oum.oumlib.bridge.region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public final class RegionBridge {

    private static boolean worldGuardChecked = false;
    private static boolean worldGuardAvailable = false;
    private static boolean townyChecked = false;
    private static boolean townyAvailable = false;
    private static boolean griefPreventionChecked = false;
    private static boolean griefPreventionAvailable = false;

    private RegionBridge() {
    }

    public static boolean isAvailable() {
        return hasWorldGuard() || hasTowny() || hasGriefPrevention();
    }

    public static boolean isPvPAllowed(@NonNull Location location) {
        return isPvPAllowed(location, null);
    }

    public static boolean isPvPAllowed(@NonNull Location location, @Nullable Player player) {
        if (hasWorldGuard()) {
            try {
                if (!checkWorldGuardPvP(location, player)) {
                    return false;
                }
            } catch (Throwable ignored) {
            }
        }

        if (hasTowny()) {
            try {
                if (!checkTownyPvP(location)) {
                    return false;
                }
            } catch (Throwable ignored) {
            }
        }

        if (hasGriefPrevention()) {
            try {
                if (!checkGriefPreventionPvP(location)) {
                    return false;
                }
            } catch (Throwable ignored) {
            }
        }

        return true;
    }

    public static boolean isSafeZone(@NonNull Location location) {
        return !isPvPAllowed(location, null);
    }

    public static boolean hasWorldGuard() {
        if (!worldGuardChecked) {
            worldGuardChecked = true;
            worldGuardAvailable = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
        }
        return worldGuardAvailable;
    }

    public static boolean hasTowny() {
        if (!townyChecked) {
            townyChecked = true;
            townyAvailable = Bukkit.getPluginManager().getPlugin("Towny") != null;
        }
        return townyAvailable;
    }

    public static boolean hasGriefPrevention() {
        if (!griefPreventionChecked) {
            griefPreventionChecked = true;
            griefPreventionAvailable = Bukkit.getPluginManager().getPlugin("GriefPrevention") != null;
        }
        return griefPreventionAvailable;
    }

    private static boolean checkWorldGuardPvP(@NonNull Location location, @Nullable Player player) throws Exception {
        Class<?> wgClass = Class.forName("com.sk89q.worldguard.WorldGuard");
        Object wgInstance = wgClass.getMethod("getInstance").invoke(null);
        Object platform = wgClass.getMethod("getPlatform").invoke(wgInstance);
        Object container = platform.getClass().getMethod("getRegionContainer").invoke(platform);
        Object query = container.getClass().getMethod("createQuery").invoke(container);

        Class<?> adapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
        Method adaptLoc = adapterClass.getMethod("adapt", Location.class);
        Object adaptedLoc = adaptLoc.invoke(null, location);

        Class<?> flagsClass = Class.forName("com.sk89q.worldguard.protection.flags.Flags");
        Object pvpFlag = flagsClass.getField("PVP").get(null);

        Object subject = null;
        if (player != null) {
            try {
                Class<?> wgPluginClass = Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
                Object wgPluginInstance = wgPluginClass.getMethod("inst").invoke(null);
                subject = wgPluginClass.getMethod("wrapPlayer", Player.class).invoke(wgPluginInstance, player);
            } catch (Throwable ignored) {
                Method adaptPlayer = adapterClass.getMethod("adapt", Player.class);
                subject = adaptPlayer.invoke(null, player);
            }
        }

        Method testState = query.getClass().getMethod("testState",
                Class.forName("com.sk89q.worldedit.util.Location"),
                Class.forName("com.sk89q.worldguard.protection.association.RegionAssociable"),
                Class.forName("com.sk89q.worldguard.protection.flags.StateFlag[]"));

        Class<?> stateFlagClass = Class.forName("com.sk89q.worldguard.protection.flags.StateFlag");
        Object flagArray = Array.newInstance(stateFlagClass, 1);
        Array.set(flagArray, 0, pvpFlag);

        Object result = testState.invoke(query, adaptedLoc, subject, flagArray);
        return Boolean.TRUE.equals(result);
    }

    private static boolean checkTownyPvP(@NonNull Location location) throws Exception {
        Class<?> townyApiClass = Class.forName("com.palmergames.bukkit.towny.TownyAPI");
        Object apiInstance = townyApiClass.getMethod("getInstance").invoke(null);
        try {
            Method isPvPMethod = townyApiClass.getMethod("isPvP", Location.class);
            return (boolean) isPvPMethod.invoke(apiInstance, location);
        } catch (NoSuchMethodException e) {
            Object town = townyApiClass.getMethod("getTown", Location.class).invoke(apiInstance, location);
            if (town == null) {
                return true;
            }
            Method isPvp = town.getClass().getMethod("isPVP");
            return (boolean) isPvp.invoke(town);
        }
    }

    private static boolean checkGriefPreventionPvP(@NonNull Location location) throws Exception {
        Class<?> gpClass = Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
        Object gpInstance = gpClass.getField("instance").get(null);
        Object dataStore = gpClass.getField("dataStore").get(gpInstance);
        Method getClaim = dataStore.getClass().getMethod("getClaimAt", Location.class, boolean.class, Class.forName("me.ryanhamshire.GriefPrevention.Claim"));
        Object claim = getClaim.invoke(dataStore, location, false, null);
        if (claim == null) {
            return true;
        }
        try {
            Method pvpMethod = claim.getClass().getMethod("isPvpEnabled");
            return (boolean) pvpMethod.invoke(claim);
        } catch (NoSuchMethodException e) {
            return true;
        }
    }
}
