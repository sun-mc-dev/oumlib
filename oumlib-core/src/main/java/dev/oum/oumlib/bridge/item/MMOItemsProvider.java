package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class MMOItemsProvider implements ItemProvider {

    public MMOItemsProvider() throws ClassNotFoundException {
        Class.forName("net.Indyuce.mmoitems.MMOItems");
    }

    @Override
    public @NonNull String name() {
        return "mmoitems";
    }

    @Override
    public @NonNull Optional<ItemStack> getItem(@NonNull String id) {
        try {
            Class<?> mmoItemsClass = Class.forName("net.Indyuce.mmoitems.MMOItems");
            Object plugin = mmoItemsClass.getField("plugin").get(null);

            String[] parts = id.split(":", 2);
            if (parts.length == 2) {
                Class<?> typeClass = Class.forName("net.Indyuce.mmoitems.api.Type");
                Object type = typeClass.getMethod("get", String.class).invoke(null, parts[0].toUpperCase());
                if (type != null) {
                    ItemStack item = (ItemStack) plugin.getClass()
                            .getMethod("getItem", typeClass, String.class)
                            .invoke(plugin, type, parts[1]);
                    return Optional.ofNullable(item);
                }
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }
}
