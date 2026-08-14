package dev.oum.oumlib.bridge.economy;

import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.util.UUID;

public final class PlayerPointsProvider implements EconomyProvider {

    public PlayerPointsProvider() {
    }

    private Object getApi() {
        try {
            Class<?> ppClass = Class.forName("org.black_ghost.playerpoints.PlayerPoints");
            Object ppInstance = ppClass.getMethod("getInstance").invoke(null);
            return ppClass.getMethod("getAPI").invoke(ppInstance);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public @NonNull String name() {
        return "playerpoints";
    }

    @Override
    public boolean isAvailable() {
        return getApi() != null;
    }

    @Override
    public boolean has(@NonNull OfflinePlayer player, double amount) {
        return balance(player) >= amount;
    }

    @Override
    public boolean withdraw(@NonNull OfflinePlayer player, double amount) {
        Object api = getApi();
        if (api == null) return false;
        try {
            int points = (int) Math.round(amount);
            Method method = api.getClass().getMethod("take", UUID.class, int.class);
            return (boolean) method.invoke(api, player.getUniqueId(), points);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deposit(@NonNull OfflinePlayer player, double amount) {
        Object api = getApi();
        if (api == null) return false;
        try {
            int points = (int) Math.round(amount);
            Method method = api.getClass().getMethod("give", UUID.class, int.class);
            return (boolean) method.invoke(api, player.getUniqueId(), points);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double balance(@NonNull OfflinePlayer player) {
        Object api = getApi();
        if (api == null) return 0.0;
        try {
            Method method = api.getClass().getMethod("look", UUID.class);
            return (int) method.invoke(api, player.getUniqueId());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
