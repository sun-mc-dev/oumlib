package dev.oum.oumlib.inventory;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DamageResistant;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DataComponents {

    private DataComponents() {
    }

    @Contract("_, _, _ -> param1")
    public static <T> @NonNull ItemStack set(@NonNull ItemStack item, DataComponentType.@NonNull Valued<T> type, @NonNull T value) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);
        item.setData(type, value);
        return item;
    }

    @CheckReturnValue
    public static <T> @NonNull Optional<T> get(@NonNull ItemStack item, DataComponentType.@NonNull Valued<T> type) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(type);
        return Optional.ofNullable(item.getData(type));
    }

    @CheckReturnValue
    public static <T> @NonNull T getOrDefault(@NonNull ItemStack item, DataComponentType.@NonNull Valued<T> type, @NonNull T defaultValue) {
        return get(item, type).orElse(defaultValue);
    }

    @CheckReturnValue
    public static boolean has(@NonNull ItemStack item, @NonNull DataComponentType type) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(type);
        return item.hasData(type);
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack remove(@NonNull ItemStack item, @NonNull DataComponentType type) {
        Objects.requireNonNull(item);
        Objects.requireNonNull(type);
        item.unsetData(type);
        return item;
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setMaxStackSize(@NonNull ItemStack item, int maxStackSize) {
        return set(item, DataComponentTypes.MAX_STACK_SIZE, maxStackSize);
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setMaxDamage(@NonNull ItemStack item, int maxDamage) {
        return set(item, DataComponentTypes.MAX_DAMAGE, maxDamage);
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setDamage(@NonNull ItemStack item, int damage) {
        return set(item, DataComponentTypes.DAMAGE, damage);
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setItemName(@NonNull ItemStack item, @NonNull Component name) {
        return set(item, DataComponentTypes.ITEM_NAME, name);
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setLore(@NonNull ItemStack item, @NonNull List<Component> lore) {
        return set(item, DataComponentTypes.LORE, ItemLore.lore(lore));
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setRarity(@NonNull ItemStack item, @NonNull ItemRarity rarity) {
        return set(item, DataComponentTypes.RARITY, rarity);
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setRepairCost(@NonNull ItemStack item, int cost) {
        return set(item, DataComponentTypes.REPAIR_COST, cost);
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setFireResistant(@NonNull ItemStack item, boolean resistant) {
        if (resistant) {
            return set(item, DataComponentTypes.DAMAGE_RESISTANT,
                    DamageResistant.damageResistant(DamageTypeTagKeys.IS_FIRE));
        } else {
            return remove(item, DataComponentTypes.DAMAGE_RESISTANT);
        }
    }

    @Contract("_, _ -> param1")
    public static @NonNull ItemStack setGlider(@NonNull ItemStack item, boolean glider) {
        if (glider) {
            item.setData(DataComponentTypes.GLIDER);
        } else {
            item.unsetData(DataComponentTypes.GLIDER);
        }
        return item;
    }
}
