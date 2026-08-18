package dev.oum.oumlib.inventory;

import org.bukkit.entity.Player;

public sealed interface Menu permits ChestMenu, PaginatedMenu, AnvilMenu, ConfirmMenu {

    void open(Player player);

    void close(Player player);

    void closeAll();
}