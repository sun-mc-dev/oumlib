package dev.oum.oumlib.bridge.item;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ItemBridge {

    private static final Map<String, ItemProvider> providers = new HashMap<>();

    static {
        registerProvider(new MinecraftProvider());

        try {
            registerProvider(new ItemsAdderProvider());
        } catch (Throwable ignored) {
        }

        try {
            registerProvider(new OraxenProvider());
        } catch (Throwable ignored) {
        }

        try {
            registerProvider(new MMOItemsProvider());
        } catch (Throwable ignored) {
        }

        try {
            registerProvider(new MythicMobsProvider());
        } catch (Throwable ignored) {
        }

        try {
            registerProvider(new NexoProvider());
        } catch (Throwable ignored) {
        }

        try {
            registerProvider(new HeadDatabaseProvider());
        } catch (Throwable ignored) {
        }

        try {
            registerProvider(new TextureHeadProvider());
        } catch (Throwable ignored) {
        }
    }

    private ItemBridge() {
    }

    public static void registerProvider(@NonNull ItemProvider provider) {
        providers.put(provider.name().toLowerCase(), provider);
    }

    public static @NonNull Optional<ItemStack> getItem(@NonNull String identifier) {
        if (identifier.isEmpty()) {
            return Optional.empty();
        }

        String[] parts = identifier.split(":", 2);
        if (parts.length == 2) {
            String namespace = parts[0].toLowerCase();
            String id = parts[1];
            ItemProvider provider = providers.get(namespace);
            if (provider != null) {
                return provider.getItem(id);
            }
        }

        ItemProvider mc = providers.get("minecraft");
        if (mc != null) {
            Optional<ItemStack> item = mc.getItem(identifier);
            if (item.isPresent()) {
                return item;
            }
        }

        for (Map.Entry<String, ItemProvider> entry : providers.entrySet()) {
            if ("minecraft".equals(entry.getKey())) continue;
            Optional<ItemStack> item = entry.getValue().getItem(identifier);
            if (item.isPresent()) {
                return item;
            }
        }

        return Optional.empty();
    }
}