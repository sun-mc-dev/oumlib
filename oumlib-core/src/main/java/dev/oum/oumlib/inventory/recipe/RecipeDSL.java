package dev.oum.oumlib.inventory.recipe;

import dev.oum.oumlib.OumLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public final class RecipeDSL {

    private RecipeDSL() {
    }

    public static @NonNull ShapedRecipeBuilder shaped(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return ShapedRecipeBuilder.of(key, result);
    }

    public static @NonNull ShapedRecipeBuilder shaped(@NonNull String key, @NonNull ItemStack result) {
        return shaped(createKey(key), result);
    }

    public static @NonNull ShapedRecipeBuilder shaped(@NonNull NamespacedKey key, @NonNull Material result) {
        return shaped(key, new ItemStack(result));
    }

    public static @NonNull ShapedRecipeBuilder shaped(@NonNull String key, @NonNull Material result) {
        return shaped(createKey(key), new ItemStack(result));
    }

    public static @NonNull ShapelessRecipeBuilder shapeless(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return ShapelessRecipeBuilder.of(key, result);
    }

    public static @NonNull ShapelessRecipeBuilder shapeless(@NonNull String key, @NonNull ItemStack result) {
        return shapeless(createKey(key), result);
    }

    public static @NonNull ShapelessRecipeBuilder shapeless(@NonNull NamespacedKey key, @NonNull Material result) {
        return shapeless(key, new ItemStack(result));
    }

    public static @NonNull ShapelessRecipeBuilder shapeless(@NonNull String key, @NonNull Material result) {
        return shapeless(createKey(key), new ItemStack(result));
    }

    public static @NonNull CookingRecipeBuilder smelting(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return CookingRecipeBuilder.smelting(key, result);
    }

    public static @NonNull CookingRecipeBuilder smelting(@NonNull String key, @NonNull ItemStack result) {
        return smelting(createKey(key), result);
    }

    public static @NonNull CookingRecipeBuilder smelting(@NonNull NamespacedKey key, @NonNull Material result) {
        return smelting(key, new ItemStack(result));
    }

    public static @NonNull CookingRecipeBuilder smelting(@NonNull String key, @NonNull Material result) {
        return smelting(createKey(key), new ItemStack(result));
    }

    public static @NonNull CookingRecipeBuilder blasting(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return CookingRecipeBuilder.blasting(key, result);
    }

    public static @NonNull CookingRecipeBuilder blasting(@NonNull String key, @NonNull ItemStack result) {
        return blasting(createKey(key), result);
    }

    public static @NonNull CookingRecipeBuilder blasting(@NonNull NamespacedKey key, @NonNull Material result) {
        return blasting(key, new ItemStack(result));
    }

    public static @NonNull CookingRecipeBuilder blasting(@NonNull String key, @NonNull Material result) {
        return blasting(createKey(key), new ItemStack(result));
    }

    public static @NonNull CookingRecipeBuilder smoking(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return CookingRecipeBuilder.smoking(key, result);
    }

    public static @NonNull CookingRecipeBuilder smoking(@NonNull String key, @NonNull ItemStack result) {
        return smoking(createKey(key), result);
    }

    public static @NonNull CookingRecipeBuilder smoking(@NonNull NamespacedKey key, @NonNull Material result) {
        return smoking(key, new ItemStack(result));
    }

    public static @NonNull CookingRecipeBuilder smoking(@NonNull String key, @NonNull Material result) {
        return smoking(createKey(key), new ItemStack(result));
    }

    public static @NonNull CookingRecipeBuilder campfire(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return CookingRecipeBuilder.campfire(key, result);
    }

    public static @NonNull CookingRecipeBuilder campfire(@NonNull String key, @NonNull ItemStack result) {
        return campfire(createKey(key), result);
    }

    public static @NonNull CookingRecipeBuilder campfire(@NonNull NamespacedKey key, @NonNull Material result) {
        return campfire(key, new ItemStack(result));
    }

    public static @NonNull CookingRecipeBuilder campfire(@NonNull String key, @NonNull Material result) {
        return campfire(createKey(key), new ItemStack(result));
    }

    public static @NonNull SmithingRecipeBuilder smithing(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return SmithingRecipeBuilder.of(key, result);
    }

    public static @NonNull SmithingRecipeBuilder smithing(@NonNull String key, @NonNull ItemStack result) {
        return smithing(createKey(key), result);
    }

    public static @NonNull SmithingRecipeBuilder smithing(@NonNull NamespacedKey key, @NonNull Material result) {
        return smithing(key, new ItemStack(result));
    }

    public static @NonNull SmithingRecipeBuilder smithing(@NonNull String key, @NonNull Material result) {
        return smithing(createKey(key), new ItemStack(result));
    }

    public static @NonNull StonecutterRecipeBuilder stonecutting(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return StonecutterRecipeBuilder.of(key, result);
    }

    public static @NonNull StonecutterRecipeBuilder stonecutting(@NonNull String key, @NonNull ItemStack result) {
        return stonecutting(createKey(key), result);
    }

    public static @NonNull StonecutterRecipeBuilder stonecutting(@NonNull NamespacedKey key, @NonNull Material result) {
        return stonecutting(key, new ItemStack(result));
    }

    public static @NonNull StonecutterRecipeBuilder stonecutting(@NonNull String key, @NonNull Material result) {
        return stonecutting(createKey(key), new ItemStack(result));
    }

    private static NamespacedKey createKey(String key) {
        if (key.contains(":")) {
            return NamespacedKey.fromString(key);
        }
        return new NamespacedKey(OumLib.plugin(), key);
    }
}
