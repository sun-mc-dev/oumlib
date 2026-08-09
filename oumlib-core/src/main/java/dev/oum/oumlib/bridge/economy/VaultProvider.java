package dev.oum.oumlib.bridge.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

public final class VaultProvider implements EconomyProvider {

    private final Object economy;

    public VaultProvider() throws Exception {
        Class<?> rspClass = Class.forName("org.bukkit.plugin.RegisteredServiceProvider");
        Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
        Object servicesManager = Bukkit.getServer().getClass().getMethod("getServicesManager").invoke(Bukkit.getServer());
        Object rsp = servicesManager.getClass().getMethod("getRegistration", Class.class).invoke(servicesManager, econClass);
        if (rsp == null) {
            throw new IllegalStateException("Vault economy provider registration not found");
        }
        this.economy = rspClass.getMethod("getProvider").invoke(rsp);
    }

    @Override
    public @NonNull String name() {
        return "vault";
    }

    @Override
    public boolean has(@NonNull OfflinePlayer player, double amount) {
        try {
            return (boolean) economy.getClass().getMethod("has", OfflinePlayer.class, double.class).invoke(economy, player, amount);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean withdraw(@NonNull OfflinePlayer player, double amount) {
        try {
            Object response = economy.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class).invoke(economy, player, amount);
            Class<?> responseClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
            Object type = responseClass.getField("type").get(response);
            return type != null && "SUCCESS".equals(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deposit(@NonNull OfflinePlayer player, double amount) {
        try {
            Object response = economy.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class).invoke(economy, player, amount);
            Class<?> responseClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
            Object type = responseClass.getField("type").get(response);
            return type != null && "SUCCESS".equals(type.toString());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public double balance(@NonNull OfflinePlayer player) {
        try {
            return (double) economy.getClass().getMethod("getBalance", OfflinePlayer.class).invoke(economy, player);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
