package dev.oum.oumlib.inventory.recipe;

import dev.oum.oumlib.OumLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class SmithingRecipeBuilder {

    private final NamespacedKey key;
    private final ItemStack result;
    private RecipeChoice template;
    private RecipeChoice base;
    private RecipeChoice addition;

    public SmithingRecipeBuilder(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        this.key = Objects.requireNonNull(key);
        this.result = Objects.requireNonNull(result);
    }

    public static @NonNull SmithingRecipeBuilder of(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return new SmithingRecipeBuilder(key, result);
    }

    public static @NonNull SmithingRecipeBuilder of(@NonNull NamespacedKey key, @NonNull Material result) {
        return new SmithingRecipeBuilder(key, new ItemStack(result));
    }

    public @NonNull SmithingRecipeBuilder template(@NonNull Material material) {
        this.template = new RecipeChoice.MaterialChoice(material);
        return this;
    }

    public @NonNull SmithingRecipeBuilder template(@NonNull ItemStack item) {
        this.template = new RecipeChoice.ExactChoice(item);
        return this;
    }

    public @NonNull SmithingRecipeBuilder template(@NonNull RecipeChoice choice) {
        this.template = Objects.requireNonNull(choice);
        return this;
    }

    public @NonNull SmithingRecipeBuilder base(@NonNull Material material) {
        this.base = new RecipeChoice.MaterialChoice(material);
        return this;
    }

    public @NonNull SmithingRecipeBuilder base(@NonNull ItemStack item) {
        this.base = new RecipeChoice.ExactChoice(item);
        return this;
    }

    public @NonNull SmithingRecipeBuilder base(@NonNull RecipeChoice choice) {
        this.base = Objects.requireNonNull(choice);
        return this;
    }

    public @NonNull SmithingRecipeBuilder addition(@NonNull Material material) {
        this.addition = new RecipeChoice.MaterialChoice(material);
        return this;
    }

    public @NonNull SmithingRecipeBuilder addition(@NonNull ItemStack item) {
        this.addition = new RecipeChoice.ExactChoice(item);
        return this;
    }

    public @NonNull SmithingRecipeBuilder addition(@NonNull RecipeChoice choice) {
        this.addition = Objects.requireNonNull(choice);
        return this;
    }

    public @NonNull SmithingTransformRecipe build() {
        if (template == null || base == null || addition == null) {
            throw new IllegalStateException("Smithing recipe requires template, base, and addition ingredients.");
        }
        return new SmithingTransformRecipe(key, result, template, base, addition);
    }

    public boolean register() {
        return OumLib.recipes().register(build());
    }
}
