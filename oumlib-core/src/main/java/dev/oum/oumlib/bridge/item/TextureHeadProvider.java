package dev.oum.oumlib.bridge.item;

import dev.oum.oumlib.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class TextureHeadProvider implements ItemProvider {

    @Override
    public @NonNull String name() {
        return "head";
    }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        if (id.isEmpty()) {
            return Optional.empty();
        }
        try {
            ItemStack skull = ItemBuilder.of(Material.PLAYER_HEAD)
                    .skull(id)
                    .build();
            return Optional.of(skull);
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }
}
