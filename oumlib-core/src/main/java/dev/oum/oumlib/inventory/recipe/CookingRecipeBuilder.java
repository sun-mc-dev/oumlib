package dev.oum.oumlib.inventory.recipe;

import dev.oum.oumlib.OumLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class CookingRecipeBuilder {

    private final Type type;
    private final NamespacedKey key;
    private final ItemStack result;
    private RecipeChoice source;
    private float experience = 0.0f;
    private int cookingTime;
    private String group;
    private CookingBookCategory category;

    public CookingRecipeBuilder(@NonNull Type type, @NonNull NamespacedKey key, @NonNull ItemStack result) {
        this.type = Objects.requireNonNull(type);
        this.key = Objects.requireNonNull(key);
        this.result = Objects.requireNonNull(result);
        this.cookingTime = type.defaultCookingTime;
    }

    public static @NonNull CookingRecipeBuilder smelting(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return new CookingRecipeBuilder(Type.SMELTING, key, result);
    }

    public static @NonNull CookingRecipeBuilder blasting(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return new CookingRecipeBuilder(Type.BLASTING, key, result);
    }

    public static @NonNull CookingRecipeBuilder smoking(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return new CookingRecipeBuilder(Type.SMOKING, key, result);
    }

    public static @NonNull CookingRecipeBuilder campfire(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return new CookingRecipeBuilder(Type.CAMPFIRE, key, result);
    }

    public @NonNull CookingRecipeBuilder source(@NonNull Material material) {
        this.source = new RecipeChoice.MaterialChoice(material);
        return this;
    }

    public @NonNull CookingRecipeBuilder source(@NonNull ItemStack item) {
        this.source = new RecipeChoice.ExactChoice(item);
        return this;
    }

    public @NonNull CookingRecipeBuilder source(@NonNull RecipeChoice choice) {
        this.source = Objects.requireNonNull(choice);
        return this;
    }

    public @NonNull CookingRecipeBuilder experience(float experience) {
        this.experience = experience;
        return this;
    }

    public @NonNull CookingRecipeBuilder cookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
        return this;
    }

    public @NonNull CookingRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public @NonNull CookingRecipeBuilder category(@Nullable CookingBookCategory category) {
        this.category = category;
        return this;
    }

    public @NonNull CookingRecipe<?> build() {
        if (source == null) {
            throw new IllegalStateException("Source ingredient must be set for cooking recipe.");
        }
        CookingRecipe<?> recipe = switch (type) {
            case SMELTING -> new FurnaceRecipe(key, result, source, experience, cookingTime);
            case BLASTING -> new BlastingRecipe(key, result, source, experience, cookingTime);
            case SMOKING -> new SmokingRecipe(key, result, source, experience, cookingTime);
            case CAMPFIRE -> new CampfireRecipe(key, result, source, experience, cookingTime);
        };
        if (group != null) {
            recipe.setGroup(group);
        }
        if (category != null) {
            recipe.setCategory(category);
        }
        return recipe;
    }

    public boolean register() {
        return OumLib.recipes().register(build());
    }

    public enum Type {
        SMELTING(200),
        BLASTING(100),
        SMOKING(100),
        CAMPFIRE(600);

        private final int defaultCookingTime;

        Type(int defaultCookingTime) {
            this.defaultCookingTime = defaultCookingTime;
        }
    }
}
