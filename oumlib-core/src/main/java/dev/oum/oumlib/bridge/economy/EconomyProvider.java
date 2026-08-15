package dev.oum.oumlib.bridge.economy;

import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

public interface EconomyProvider {

    @NonNull String name();

    default boolean isAvailable() {
        return true;
    }

    boolean has(@NonNull OfflinePlayer player, double amount);

    boolean withdraw(@NonNull OfflinePlayer player, double amount);

    boolean deposit(@NonNull OfflinePlayer player, double amount);

    double balance(@NonNull OfflinePlayer player);
}
