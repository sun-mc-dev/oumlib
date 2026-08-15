package dev.oum.oumlib.inventory.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RecipeRegistry {

    private final Set<NamespacedKey> registeredKeys = ConcurrentHashMap.newKeySet();

    public synchronized boolean register(@NonNull Recipe recipe) {
        if (recipe instanceof Keyed keyed) {
            NamespacedKey key = keyed.getKey();
            if (Bukkit.getRecipe(key) != null) {
                Bukkit.removeRecipe(key);
            }
            boolean success = Bukkit.addRecipe(recipe);
            if (success) {
                registeredKeys.add(key);
            }
            return success;
        }
        return Bukkit.addRecipe(recipe);
    }

    public synchronized boolean unregister(@NonNull NamespacedKey key) {
        registeredKeys.remove(key);
        return Bukkit.removeRecipe(key);
    }

    public @Nullable Recipe get(@NonNull NamespacedKey key) {
        return Bukkit.getRecipe(key);
    }

    @Contract(pure = true)
    public @NonNull @UnmodifiableView Set<NamespacedKey> getRegisteredKeys() {
        return Collections.unmodifiableSet(registeredKeys);
    }

    public void discover(@NonNull Player player, @NonNull NamespacedKey... keys) {
        player.discoverRecipes(Arrays.asList(keys));
    }

    public void undiscover(@NonNull Player player, @NonNull NamespacedKey... keys) {
        player.undiscoverRecipes(Arrays.asList(keys));
    }

    public synchronized void unregisterAll() {
        for (NamespacedKey key : registeredKeys) {
            Bukkit.removeRecipe(key);
        }
        registeredKeys.clear();
    }
}
