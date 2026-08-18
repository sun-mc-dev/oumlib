package dev.oum.oumlib.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Layout {

    private final String[] pattern;
    private final Map<Character, Function<Player, ItemStack>> bindings = new HashMap<>();
    private final Map<Character, List<Integer>> slotMap = new HashMap<>();

    public Layout(String @NonNull ... rows) {
        if (rows.length > 6) throw new IllegalArgumentException("Max 6 rows.");
        this.pattern = rows;
        buildSlotMap();
    }

    public Layout bind(char key, @Nullable ItemStack item) {
        bindings.put(key, player -> item);
        return this;
    }

    public Layout bind(char key, @NonNull Supplier<@Nullable ItemStack> supplier) {
        bindings.put(key, player -> supplier.get());
        return this;
    }

    public Layout bind(char key, @NonNull Function<@NonNull Player, @Nullable ItemStack> function) {
        bindings.put(key, function);
        return this;
    }

    public @NonNull String[] pattern() {
        return pattern;
    }

    public @NonNull Set<Character> characters() {
        return slotMap.keySet();
    }

    public List<Integer> slotsFor(char key) {
        return slotMap.getOrDefault(key, List.of());
    }

    public void apply(@NonNull Inventory inventory, @NonNull Player player) {
        bindings.forEach((key, function) ->
                slotsFor(key).forEach(slot -> inventory.setItem(slot, function.apply(player)))
        );
    }

    @Deprecated(since = "1.0.4")
    public void apply(@NonNull Inventory inventory) {
        bindings.forEach((key, function) ->
                slotsFor(key).forEach(slot -> inventory.setItem(slot, function.apply(null)))
        );
    }

    private void buildSlotMap() {
        for (int row = 0; row < pattern.length; row++) {
            String line = pattern[row];
            for (int col = 0; col < Math.min(line.length(), 9); col++) {
                char c = line.charAt(col);
                int slot = row * 9 + col;
                slotMap.computeIfAbsent(c, k -> new ArrayList<>()).add(slot);
            }
        }
    }
}