package dev.oum.oumlib.inventory;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.Gson;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.bridge.item.ItemBridge;
import dev.oum.oumlib.pdc.DataKey;
import dev.oum.oumlib.pdc.PdcModel;
import dev.oum.oumlib.text.Text;
import io.papermc.paper.datacomponent.DataComponentType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ItemBuilder {

    private static final Gson GSON = new Gson();
    private final List<Consumer<ItemStack>> dataModifiers = new ArrayList<>();
    private final ItemMeta meta;
    private ItemStack stack;

    private ItemBuilder(Material material) {
        this.stack = new ItemStack(material);
        this.meta = stack.getItemMeta();
    }

    private ItemBuilder(@NonNull ItemStack item) {
        this.stack = item.clone();
        this.meta = stack.getItemMeta();
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull ItemBuilder of(@NonNull Material material) {
        return new ItemBuilder(material);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull ItemBuilder of(@NonNull ItemStack item) {
        return new ItemBuilder(item);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull ItemBuilder from(@NonNull String identifier) {
        return from(identifier, Material.STONE);
    }

    @Contract("_, _ -> new")
    @CheckReturnValue
    public static @NonNull ItemBuilder from(@NonNull String identifier, @NonNull Material fallback) {
        return ItemBridge.getItem(identifier)
                .map(ItemBuilder::of)
                .orElseGet(() -> {
                    Material mat = Material.matchMaterial(identifier);
                    return of(mat != null ? mat : fallback);
                });
    }

    @Contract("_, _, _ -> new")
    @CheckReturnValue
    public static @NonNull ItemStack quick(@NonNull Material material, @NonNull String miniMessageName, String @NonNull ... loreLines) {
        return of(material).name(miniMessageName).lore(loreLines).build();
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder name(@NonNull String miniMessage) {
        meta.displayName(Text.parse("<!italic>" + miniMessage));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder name(@Nullable Component component) {
        meta.displayName(component);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder lore(String @NonNull ... lines) {
        meta.lore(Arrays.stream(lines)
                .map(l -> Text.parse("<!italic><gray>" + l))
                .collect(Collectors.toList()));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder lore(Component @NonNull ... lines) {
        meta.lore(Arrays.asList(lines));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder lore(@Nullable List<Component> lines) {
        meta.lore(lines);
        return this;
    }

    @Contract(value = "-> this", mutates = "this")
    public @NonNull ItemBuilder clearLore() {
        meta.lore(null);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder addLore(String @NonNull ... lines) {
        List<Component> currentLore = meta.lore();
        if (currentLore == null) currentLore = new ArrayList<>();
        List<Component> newLines = Arrays.stream(lines)
                .map(l -> Text.parse("<!italic><gray>" + l))
                .toList();
        currentLore.addAll(newLines);
        meta.lore(currentLore);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder addLore(Component @NonNull ... lines) {
        List<Component> currentLore = meta.lore();
        if (currentLore == null) currentLore = new ArrayList<>();
        currentLore.addAll(Arrays.asList(lines));
        meta.lore(currentLore);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder type(@NonNull Material material) {
        stack = stack.withType(material);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder enchant(@NonNull Enchantment e, int level) {
        meta.addEnchant(e, level, true);
        return this;
    }

    @Contract(value = "-> this", mutates = "this")
    public @NonNull ItemBuilder glow() {
        return glow(true);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder glow(boolean glow) {
        meta.setEnchantmentGlintOverride(glow);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder flag(ItemFlag @NonNull ... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    @SuppressWarnings("deprecation")
    public @NonNull ItemBuilder customModelData(@Nullable Integer data) {
        meta.setCustomModelData(data);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder modelData(int data) {
        return customModelData(data);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder unbreakable(boolean value) {
        meta.setUnbreakable(value);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder itemModel(@NonNull Key key) {
        meta.setItemModel(NamespacedKey.fromString(key.asString()));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder itemModel(@NonNull NamespacedKey key) {
        meta.setItemModel(key);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder glintOverride(boolean glint) {
        meta.setEnchantmentGlintOverride(glint);
        return this;
    }


    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder hideTooltip(boolean hide) {
        meta.setHideTooltip(hide);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NonNull ItemBuilder data(DataComponentType.@NonNull Valued type, @NonNull Object value) {
        dataModifiers.add(s -> s.setData(type, value));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder removeData(@NonNull DataComponentType type) {
        dataModifiers.add(s -> s.unsetData(type));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder rarity(@NonNull ItemRarity rarity) {
        dataModifiers.add(s -> DataComponents.setRarity(s, rarity));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder maxStackSize(int maxStackSize) {
        dataModifiers.add(s -> DataComponents.setMaxStackSize(s, maxStackSize));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder maxDamage(int maxDamage) {
        dataModifiers.add(s -> DataComponents.setMaxDamage(s, maxDamage));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder damage(int damage) {
        dataModifiers.add(s -> DataComponents.setDamage(s, damage));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder itemName(@NonNull Component itemName) {
        dataModifiers.add(s -> DataComponents.setItemName(s, itemName));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder repairCost(int cost) {
        dataModifiers.add(s -> DataComponents.setRepairCost(s, cost));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder fireResistant(boolean fireResistant) {
        dataModifiers.add(s -> DataComponents.setFireResistant(s, fireResistant));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <T extends Record> @NonNull ItemBuilder pdc(@NonNull T recordInstance) {
        PdcModel.write(meta.getPersistentDataContainer(), recordInstance);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public <P, C> @NonNull ItemBuilder pdc(@NonNull DataKey<P, C> key, @NonNull C value) {
        meta.getPersistentDataContainer().set(key.key(), key.type(), value);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, @NonNull String value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, value);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, int value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.INTEGER, value);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, double value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.DOUBLE, value);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, boolean value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, long value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.LONG, value);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, @Nullable List<String> value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        if (value == null) {
            meta.getPersistentDataContainer().remove(nsk);
        } else {
            meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, GSON.toJson(value));
        }
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, @Nullable ItemStack value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        if (value == null) {
            meta.getPersistentDataContainer().remove(nsk);
        } else {
            meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, ItemSerializer.serialize(value));
        }
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, ItemStack @Nullable [] value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        if (value == null) {
            meta.getPersistentDataContainer().remove(nsk);
        } else {
            meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, ItemSerializer.serializeArray(value));
        }
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ItemBuilder pdc(@NonNull String key, @Nullable Component value) {
        NamespacedKey nsk = new NamespacedKey(OumLib.plugin(), key);
        if (value == null) {
            meta.getPersistentDataContainer().remove(nsk);
        } else {
            meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, Text.serialize(value));
        }
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder skull(@NonNull OfflinePlayer player) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ItemBuilder skull(@NonNull String textureValue) {
        if (meta instanceof SkullMeta skullMeta) {
            try {
                PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
                PlayerTextures textures = profile.getTextures();

                String targetUrl = textureValue;
                if (textureValue.startsWith("ey")) {
                    try {
                        String decodedStr = new String(Base64.getDecoder().decode(textureValue), StandardCharsets.UTF_8);
                        int index = decodedStr.indexOf("\"url\":\"");
                        if (index != -1) {
                            int start = index + 7;
                            int end = decodedStr.indexOf("\"", start);
                            if (end != -1) {
                                targetUrl = decodedStr.substring(start, end);
                            }
                        }
                    } catch (Throwable ignored) {
                    }
                }

                if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                    targetUrl = "https://textures.minecraft.net/texture/" + targetUrl;
                }

                URL url = URI.create(targetUrl).toURL();
                textures.setSkin(url);
                profile.setTextures(textures);
                skullMeta.setPlayerProfile(profile);
            } catch (Throwable ignored) {
            }
        }
        return this;
    }

    @Contract(" -> new")
    @CheckReturnValue
    public @NonNull ItemStack build() {
        stack.setItemMeta(meta);
        for (Consumer<ItemStack> modifier : dataModifiers) {
            modifier.accept(stack);
        }
        return stack;
    }
}