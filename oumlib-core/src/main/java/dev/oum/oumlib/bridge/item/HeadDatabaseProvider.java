package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class HeadDatabaseProvider implements ItemProvider {

    private final Object api;

    public HeadDatabaseProvider() throws Exception {
        Class<?> apiClass = Class.forName("me.arcaniax.hdb.api.HeadDatabaseAPI");
        this.api = apiClass.getDeclaredConstructor().newInstance();
    }

    @Override
    public @NonNull String name() {
        return "hdb";
    }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        try {
            ItemStack item = (ItemStack) api.getClass().getMethod("getItemHead", String.class).invoke(api, id);
            return Optional.ofNullable(item);
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}