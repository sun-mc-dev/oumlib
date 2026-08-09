package dev.oum.oumlib.inventory.recipe;

import dev.oum.oumlib.OumLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class StonecutterRecipeBuilder {

    private final NamespacedKey key;
    private final ItemStack result;
    private RecipeChoice source;
    private String group;

    public StonecutterRecipeBuilder(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        this.key = Objects.requireNonNull(key);
        this.result = Objects.requireNonNull(result);
    }

    public static @NonNull StonecutterRecipeBuilder of(@NonNull NamespacedKey key, @NonNull ItemStack result) {
        return new StonecutterRecipeBuilder(key, result);
    }

    public static @NonNull StonecutterRecipeBuilder of(@NonNull NamespacedKey key, @NonNull Material result) {
        return new StonecutterRecipeBuilder(key, new ItemStack(result));
    }

    public @NonNull StonecutterRecipeBuilder source(@NonNull Material material) {
        this.source = new RecipeChoice.MaterialChoice(material);
        return this;
    }

    public @NonNull StonecutterRecipeBuilder source(@NonNull ItemStack item) {
        this.source = new RecipeChoice.ExactChoice(item);
        return this;
    }

    public @NonNull StonecutterRecipeBuilder source(@NonNull RecipeChoice choice) {
        this.source = Objects.requireNonNull(choice);
        return this;
    }

    public @NonNull StonecutterRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public @NonNull StonecuttingRecipe build() {
        if (source == null) {
            throw new IllegalStateException("Source ingredient must be set for stonecutting recipe.");
        }
        StonecuttingRecipe recipe = new StonecuttingRecipe(key, result, source);
        if (group != null) {
            recipe.setGroup(group);
        }
        return recipe;
    }

    public boolean register() {
        return OumLib.recipes().register(build());
    }
}
