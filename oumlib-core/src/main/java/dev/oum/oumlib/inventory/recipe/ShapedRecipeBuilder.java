package dev.oum.oumlib.inventory.recipe;

import dev.oum.oumlib.OumLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ShapedRecipeBuilder {

    private final NamespacedKey key;
    private final ItemStack result;
    private final Map<Character, RecipeChoice> ingredients = new HashMap<>();
    private String[] shape;
    private String group;
    private CraftingBookCategory category;

    public ShapedRecipeBuilder(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        this.key = Objects.requireNonNull(key);
        this.result = Objects.requireNonNull(result);
    }

    public static @NonNull ShapedRecipeBuilder of(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return new ShapedRecipeBuilder(key, result);
    }

    public static @NonNull ShapedRecipeBuilder of(@NonNull NamespacedKey key, @NonNull Material result) {
        return new ShapedRecipeBuilder(key, new ItemStack(result));
    }

    public @NonNull ShapedRecipeBuilder shape(@NonNull String... shape) {
        this.shape = shape;
        return this;
    }

    public @NonNull ShapedRecipeBuilder set(char key, @NonNull Material material) {
        this.ingredients.put(key, new RecipeChoice.MaterialChoice(material));
        return this;
    }

    public @NonNull ShapedRecipeBuilder set(char key, @NonNull ItemStack item) {
        this.ingredients.put(key, new RecipeChoice.ExactChoice(item));
        return this;
    }

    public @NonNull ShapedRecipeBuilder set(char key, @NonNull RecipeChoice choice) {
        this.ingredients.put(key, Objects.requireNonNull(choice));
        return this;
    }

    public @NonNull ShapedRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public @NonNull ShapedRecipeBuilder category(@Nullable CraftingBookCategory category) {
        this.category = category;
        return this;
    }

    public @NonNull ShapedRecipe build() {
        if (shape == null || shape.length == 0) {
            throw new IllegalStateException("Recipe shape must be defined.");
        }
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape);
        ingredients.forEach(recipe::setIngredient);
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
}
