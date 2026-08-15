package dev.oum.oumlib.inventory.recipe;

import dev.oum.oumlib.OumLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ShapelessRecipeBuilder {

    private final NamespacedKey key;
    private final ItemStack result;
    private final List<RecipeChoice> ingredients = new ArrayList<>();
    private String group;
    private CraftingBookCategory category;

    public ShapelessRecipeBuilder(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        this.key = Objects.requireNonNull(key);
        this.result = Objects.requireNonNull(result);
    }

    public static @NonNull ShapelessRecipeBuilder of(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return new ShapelessRecipeBuilder(key, result);
    }

    public static @NonNull ShapelessRecipeBuilder of(@NonNull NamespacedKey key, @NonNull Material result) {
        return new ShapelessRecipeBuilder(key, new ItemStack(result));
    }

    public @NonNull ShapelessRecipeBuilder add(@NonNull Material material) {
        return add(material, 1);
    }

    public @NonNull ShapelessRecipeBuilder add(@NonNull Material material, int count) {
        for (int i = 0; i < count; i++) {
            this.ingredients.add(new RecipeChoice.MaterialChoice(material));
        }
        return this;
    }

    public @NonNull ShapelessRecipeBuilder add(@NonNull ItemStack item) {
        return add(item, 1);
    }

    public @NonNull ShapelessRecipeBuilder add(@NonNull ItemStack item, int count) {
        for (int i = 0; i < count; i++) {
            this.ingredients.add(new RecipeChoice.ExactChoice(item));
        }
        return this;
    }

    public @NonNull ShapelessRecipeBuilder add(@NonNull RecipeChoice choice) {
        return add(choice, 1);
    }

    public @NonNull ShapelessRecipeBuilder add(@NonNull RecipeChoice choice, int count) {
        for (int i = 0; i < count; i++) {
            this.ingredients.add(choice);
        }
        return this;
    }

    public @NonNull ShapelessRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public @NonNull ShapelessRecipeBuilder category(@Nullable CraftingBookCategory category) {
        this.category = category;
        return this;
    }

    public @NonNull ShapelessRecipe build() {
        if (ingredients.isEmpty()) {
            throw new IllegalStateException("Shapeless recipe must have at least one ingredient.");
        }
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        ingredients.forEach(recipe::addIngredient);
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
