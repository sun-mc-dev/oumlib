package dev.oum.oumlib.bridge.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;

public final class VaultProvider implements EconomyProvider {

    public VaultProvider() {
    }

    private Object getEconomy() {
        try {
            Class<?> rspClass = Class.forName("org.bukkit.plugin.RegisteredServiceProvider");
            Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
            Object servicesManager = Bukkit.getServer().getClass().getMethod("getServicesManager").invoke(Bukkit.getServer());
            Object rsp = servicesManager.getClass().getMethod("getRegistration", Class.class).invoke(servicesManager, econClass);
            if (rsp == null) {
                return null;
            }
            return rspClass.getMethod("getProvider").invoke(rsp);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public @NonNull String name() {
        return "vault";
    }

    @Override
    public boolean isAvailable() {
        return getEconomy() != null;
    }

    @Override
    public boolean has(@NonNull OfflinePlayer player, double amount) {
        Object economy = getEconomy();
        if (economy == null) return false;
        try {
            Method method = economy.getClass().getMethod("has", OfflinePlayer.class, double.class);
            return (boolean) method.invoke(economy, player, amount);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean withdraw(@NonNull OfflinePlayer player, double amount) {
        Object economy = getEconomy();
        if (economy == null) return false;
        try {
            Method method = economy.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            Object response = method.invoke(economy, player, amount);
            Class<?> responseClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
            Object type = responseClass.getField("type").get(response);
            return type != null && "SUCCESS".equals(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deposit(@NonNull OfflinePlayer player, double amount) {
        Object economy = getEconomy();
        if (economy == null) return false;
        try {
            Method method = economy.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class);
            Object response = method.invoke(economy, player, amount);
            Class<?> responseClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
            Object type = responseClass.getField("type").get(response);
            return type != null && "SUCCESS".equals(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double balance(@NonNull OfflinePlayer player) {
        Object economy = getEconomy();
        if (economy == null) return 0.0;
        try {
            Method method = economy.getClass().getMethod("getBalance", OfflinePlayer.class);
            return (double) method.invoke(economy, player);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
