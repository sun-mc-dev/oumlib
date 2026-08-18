package dev.oum.oumlib.inventory;

import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.event.ListenerHandle;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ChestMenu implements Menu {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final String title;
    private final int rows;
    private final Layout layout;
    private final Map<Integer, Consumer<ClickContext>> slotHandlers;
    private final Map<Integer, Function<Player, ItemStack>> slotItems;
    private final Map<String, Function<Player, Object>> stateProviders;
    private final Map<UUID, Map<String, Object>> playerStates = new ConcurrentHashMap<>();
    private final List<Integer> unlockedSlots;
    private final Consumer<Player> onClose;
    private final Map<UUID, Inventory> open = new ConcurrentHashMap<>();
    private final Sound openSound;
    private final Sound closeSound;
    private final Sound clickSound;
    private final Duration autoRefresh;

    private ListenerHandle clickHandle;
    private ListenerHandle dragHandle;
    private ListenerHandle closeHandle;
    private TaskHandle refreshTask;

    private ChestMenu(@NonNull Builder builder) {
        this.title = builder.title;
        this.rows = builder.rows;
        this.layout = builder.layout;
        this.slotHandlers = Map.copyOf(builder.slotHandlers);
        this.slotItems = Map.copyOf(builder.slotItems);
        this.stateProviders = Map.copyOf(builder.stateProviders);
        this.unlockedSlots = List.copyOf(builder.unlockedSlots);
        this.onClose = builder.onClose;
        this.openSound = builder.openSound;
        this.closeSound = builder.closeSound;
        this.clickSound = builder.clickSound;
        this.autoRefresh = builder.autoRefresh;
    }

    @Contract(" -> new")
    @CheckReturnValue
    public static @NonNull Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T getState(@NonNull Player player, @NonNull String key) {
        Map<String, Object> state = playerStates.get(player.getUniqueId());
        return state != null ? (T) state.get(key) : null;
    }

    public void updateState(@NonNull Player player, @NonNull String key, @Nullable Object value) {
        Scheduler.runFor(player, () -> {
            Map<String, Object> state = playerStates.get(player.getUniqueId());
            if (state != null) {
                state.put(key, value);
                String resolvedTitle = title;
                for (Map.Entry<String, Object> entry : state.entrySet()) {
                    resolvedTitle = resolvedTitle.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                }
                try {
                    var view = player.getOpenInventory();
                    try {
                        var method = view.getClass().getMethod("setTitle", Component.class);
                        method.invoke(view, MM.deserialize(resolvedTitle));
                    } catch (NoSuchMethodException e) {
                        view.setTitle(resolvedTitle);
                    }
                } catch (Throwable ignored) {
                }
                refresh(player);
            }
        });
    }

    @Override
    public void open(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            Map<String, Object> state = playerStates.computeIfAbsent(player.getUniqueId(), uuid -> {
                Map<String, Object> map = new HashMap<>();
                stateProviders.forEach((key, provider) -> map.put(key, provider.apply(player)));
                return map;
            });

            String resolvedTitle = title;
            for (Map.Entry<String, Object> entry : state.entrySet()) {
                resolvedTitle = resolvedTitle.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }

            Inventory inv = Bukkit.createInventory(null, rows * 9, MM.deserialize(resolvedTitle));
            if (layout != null) layout.apply(inv, player);
            slotItems.forEach((slot, function) -> inv.setItem(slot, function.apply(player)));
            open.put(player.getUniqueId(), inv);
            registerListeners();
            player.openInventory(inv);
            if (openSound != null) {
                player.playSound(openSound);
            }
            if (autoRefresh != null && refreshTask == null) {
                refreshTask = Scheduler.runRepeating(autoRefresh, autoRefresh, () -> {
                    for (UUID uuid : open.keySet()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null && p.isOnline()) {
                            refresh(p);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void close(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            open.remove(player.getUniqueId());
            playerStates.remove(player.getUniqueId());
            player.closeInventory();
            if (closeSound != null) {
                player.playSound(closeSound);
            }
            if (open.isEmpty()) {
                unregisterListeners();
                if (refreshTask != null) {
                    refreshTask.cancel();
                    refreshTask = null;
                }
            }
        });
    }

    public void refresh(@NonNull Player player) {
        Scheduler.runFor(player, () -> {
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;
            if (layout != null) layout.apply(inv, player);
            slotItems.forEach((slot, function) -> inv.setItem(slot, function.apply(player)));
            player.updateInventory();
        });
    }

    public void setItem(@NonNull Player player, int slot, ItemStack item) {
        Scheduler.runFor(player, () -> {
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;
            inv.setItem(slot, item);
            player.updateInventory();
        });
    }

    private @Nullable ItemStack moveItemToUnlockedSlots(Inventory inv, @NonNull ItemStack toMove) {
        ItemStack stack = toMove.clone();
        for (int slot : unlockedSlots) {
            ItemStack existing = inv.getItem(slot);
            if (existing != null && !existing.getType().isAir() && existing.isSimilar(stack)) {
                int room = existing.getMaxStackSize() - existing.getAmount();
                if (room > 0) {
                    int toAdd = Math.min(room, stack.getAmount());
                    existing.setAmount(existing.getAmount() + toAdd);
                    stack.setAmount(stack.getAmount() - toAdd);
                    if (stack.getAmount() <= 0) {
                        return null;
                    }
                }
            }
        }
        for (int slot : unlockedSlots) {
            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                inv.setItem(slot, stack);
                return null;
            }
        }
        return stack;
    }

    private synchronized void registerListeners() {
        if (clickHandle != null && clickHandle.isActive()) return;
        MenuRegistry.register(this);

        clickHandle = Events.listen(InventoryClickEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;

            if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
                return;
            }

            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                if (event.getClickedInventory() != null && !event.getClickedInventory().equals(inv)) {
                    event.setCancelled(true);
                    ItemStack toMove = event.getCurrentItem();
                    if (toMove != null && !toMove.getType().isAir()) {
                        ItemStack remaining = moveItemToUnlockedSlots(inv, toMove);
                        event.setCurrentItem(remaining);
                        player.updateInventory();
                    }
                    return;
                }
            }

            if (event.getClickedInventory() == null || !event.getClickedInventory().equals(inv)) return;

            int slot = event.getSlot();
            if (!unlockedSlots.contains(slot)) {
                event.setCancelled(true);
                if (clickSound != null) {
                    player.playSound(clickSound);
                }
                Consumer<ClickContext> handler = slotHandlers.get(slot);
                if (handler != null) {
                    handler.accept(new ClickContext(player, ClickAction.from(event.getClick()), slot, this));
                }
            } else {
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                        event.getAction() == InventoryAction.HOTBAR_SWAP) {
                    event.setCancelled(true);
                }
            }
        });

        dragHandle = Events.listen(InventoryDragEvent.class, event -> {
            if (!(event.getWhoClicked() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv == null) return;
            if (!event.getInventory().equals(inv)) return;
            boolean touchesLocked = event.getRawSlots().stream()
                    .anyMatch(s -> s < inv.getSize() && !unlockedSlots.contains(s));
            if (touchesLocked) {
                event.setCancelled(true);
            }
        });

        closeHandle = Events.listen(InventoryCloseEvent.class, event -> {
            if (!(event.getPlayer() instanceof Player player)) return;
            Inventory inv = open.get(player.getUniqueId());
            if (inv != null && event.getInventory().equals(inv)) {
                if (onClose != null) {
                    onClose.accept(player);
                }
                open.remove(player.getUniqueId());
                playerStates.remove(player.getUniqueId());
                if (open.isEmpty()) {
                    unregisterListeners();
                    if (refreshTask != null) {
                        refreshTask.cancel();
                        refreshTask = null;
                    }
                }
            }
        });
    }

    private synchronized void unregisterListeners() {
        MenuRegistry.unregister(this);
        if (clickHandle != null) {
            clickHandle.unregister();
            clickHandle = null;
        }
        if (dragHandle != null) {
            dragHandle.unregister();
            dragHandle = null;
        }
        if (closeHandle != null) {
            closeHandle.unregister();
            closeHandle = null;
        }
    }

    public void closeAll() {
        new ArrayList<>(open.keySet()).forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.closeInventory();
            }
        });
        open.clear();
        playerStates.clear();
        unregisterListeners();
    }

    public static final class Builder {

        private final Map<Integer, Consumer<ClickContext>> slotHandlers = new HashMap<>();
        private final Map<Integer, Function<Player, ItemStack>> slotItems = new HashMap<>();
        private final Map<String, Function<Player, Object>> stateProviders = new HashMap<>();
        private final List<Integer> unlockedSlots = new ArrayList<>();
        private String title = "<gray>Menu";
        private int rows = 3;
        private Layout layout;
        private Consumer<Player> onClose;
        private Sound openSound;
        private Sound closeSound;
        private Sound clickSound;
        private Duration autoRefresh;

        @CheckReturnValue
        public @NonNull Builder autoRefresh(@Nullable Duration duration) {
            this.autoRefresh = duration;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder openSound(@Nullable Sound sound) {
            this.openSound = sound;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder closeSound(@Nullable Sound sound) {
            this.closeSound = sound;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder clickSound(@Nullable Sound sound) {
            this.clickSound = sound;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder title(@NonNull String title) {
            this.title = title;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder rows(int rows) {
            if (rows < 1 || rows > 6) throw new IllegalArgumentException("Rows must be 1–6.");
            this.rows = rows;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder pattern(String @NonNull ... rows) {
            this.layout = new Layout(rows);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder state(@NonNull String key, @NonNull Function<@NonNull Player, @Nullable Object> provider) {
            this.stateProviders.put(key, provider);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder state(@NonNull String key, @NonNull Object initialValue) {
            this.stateProviders.put(key, player -> initialValue);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder bind(char key, @Nullable ItemStack item) {
            if (layout != null) layout.bind(key, item);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder bind(char key, @NonNull Supplier<@Nullable ItemStack> supplier) {
            if (layout != null) layout.bind(key, supplier);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder bind(char key, @NonNull Function<@NonNull Player, @Nullable ItemStack> function) {
            if (layout != null) layout.bind(key, function);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder bindBorders(@Nullable ItemStack item, char... ignoredKeys) {
            if (layout != null) {
                Set<Character> ignored = new HashSet<>();
                for (char c : ignoredKeys) {
                    ignored.add(c);
                }
                for (char c : layout.characters()) {
                    if (!ignored.contains(c)) {
                        layout.bind(c, item);
                    }
                }
            }
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder bindBorders(@NonNull Supplier<@Nullable ItemStack> supplier, char... ignoredKeys) {
            if (layout != null) {
                Set<Character> ignored = new HashSet<>();
                for (char c : ignoredKeys) {
                    ignored.add(c);
                }
                for (char c : layout.characters()) {
                    if (!ignored.contains(c)) {
                        layout.bind(c, supplier);
                    }
                }
            }
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder fill(@Nullable ItemStack item) {
            int totalSlots = rows * 9;
            for (int i = 0; i < totalSlots; i++) {
                final int slot = i;
                if (!slotItems.containsKey(slot)) {
                    slotItems.put(slot, player -> item);
                }
            }
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder item(int slot, @Nullable ItemStack item) {
            this.slotItems.put(slot, player -> item);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder item(int slot, @NonNull Supplier<@Nullable ItemStack> supplier) {
            this.slotItems.put(slot, player -> supplier.get());
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder item(int slot, @NonNull Function<@NonNull Player, @Nullable ItemStack> function) {
            this.slotItems.put(slot, function);
            return this;
        }

        @Contract("_ -> this")
        @CheckReturnValue
        public @NonNull Builder unlock(int @NonNull ... slots) {
            for (int s : slots) {
                this.unlockedSlots.add(s);
            }
            return this;
        }

        @Contract("_ -> this")
        @CheckReturnValue
        public @NonNull Builder unlock(@NonNull List<Integer> slots) {
            this.unlockedSlots.addAll(slots);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onClose(@Nullable Consumer<Player> onClose) {
            this.onClose = onClose;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onClick(int slot, @NonNull Consumer<ClickContext> handler) {
            slotHandlers.put(slot, handler);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onClick(char key, @NonNull Consumer<ClickContext> handler) {
            if (layout != null) {
                layout.slotsFor(key).forEach(slot -> slotHandlers.put(slot, handler));
            }
            return this;
        }

        @Contract("_, _ -> this")
        @CheckReturnValue
        public @NonNull Builder onClick(@NonNull List<Integer> slots, @NonNull Consumer<ClickContext> handler) {
            slots.forEach(slot -> slotHandlers.put(slot, handler));
            return this;
        }

        @Contract(" -> new")
        public @NonNull ChestMenu build() {
            return new ChestMenu(this);
        }
    }
}