package dev.oum.oumlib.inventory;

import dev.oum.oumlib.scheduler.Scheduler;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ConfirmMenu implements Menu {

    private final ChestMenu chestMenu;

    private ConfirmMenu(@NonNull Builder builder) {
        ChestMenu.Builder cb = ChestMenu.builder()
                .title(builder.title)
                .rows(builder.rows);

        if (builder.pattern != null && builder.pattern.length > 0) {
            cb.pattern(builder.pattern);
        } else {
            cb.pattern(
                    "#########",
                    "  C   D  ",
                    "#########"
            );
        }

        if (builder.openSound != null) cb.openSound(builder.openSound);
        if (builder.closeSound != null) cb.closeSound(builder.closeSound);
        if (builder.clickSound != null) cb.clickSound(builder.clickSound);

        if (builder.borderItem != null) {
            cb.bindBorders(builder.borderItem, builder.confirmSlotChar, builder.denySlotChar);
        }

        cb.bind(builder.confirmSlotChar, builder.confirmItem);
        cb.onClick(builder.confirmSlotChar, ctx -> {
            Player p = ctx.player();
            p.closeInventory();
            if (builder.confirmSound != null) p.playSound(builder.confirmSound);
            if (builder.onConfirm != null) {
                Scheduler.runFor(p, () -> builder.onConfirm.accept(p));
            }
        });

        cb.bind(builder.denySlotChar, builder.denyItem);
        cb.onClick(builder.denySlotChar, ctx -> {
            Player p = ctx.player();
            p.closeInventory();
            if (builder.denySound != null) p.playSound(builder.denySound);
            if (builder.onDeny != null) {
                Scheduler.runFor(p, () -> builder.onDeny.accept(p));
            }
        });

        if (builder.onClose != null) {
            cb.onClose(builder.onClose);
        }

        this.chestMenu = cb.build();
    }

    @Contract(" -> new")
    @CheckReturnValue
    public static @NonNull Builder builder() {
        return new Builder();
    }

    @Override
    public void open(@NonNull Player player) {
        chestMenu.open(player);
    }

    @Override
    public void close(@NonNull Player player) {
        chestMenu.close(player);
    }

    @Override
    public void closeAll() {
        chestMenu.closeAll();
    }

    public static final class Builder {
        private String title = "<red>Are you sure?";
        private int rows = 3;
        private String[] pattern;
        private char confirmSlotChar = 'C';
        private char denySlotChar = 'D';

        private Function<Player, ItemStack> confirmItem = p -> ItemBuilder.of(Material.LIME_CONCRETE)
                .name("<green><b>Confirm</b>")
                .build();
        private Function<Player, ItemStack> denyItem = p -> ItemBuilder.of(Material.RED_CONCRETE)
                .name("<red><b>Cancel</b>")
                .build();
        private ItemStack borderItem = ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();

        private Consumer<Player> onConfirm;
        private Consumer<Player> onDeny;
        private Consumer<Player> onClose;

        private Sound confirmSound;
        private Sound denySound;
        private Sound openSound;
        private Sound closeSound;
        private Sound clickSound;

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
            this.pattern = rows;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder pattern(@NonNull List<String> rows) {
            this.pattern = rows.toArray(new String[0]);
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder confirmSlot(char key) {
            this.confirmSlotChar = key;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder denySlot(char key) {
            this.denySlotChar = key;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder confirmItem(@Nullable ItemStack item) {
            this.confirmItem = p -> item;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder confirmItem(@NonNull Supplier<@Nullable ItemStack> supplier) {
            this.confirmItem = p -> supplier.get();
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder confirmItem(@NonNull Function<@NonNull Player, @Nullable ItemStack> function) {
            this.confirmItem = function;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder denyItem(@Nullable ItemStack item) {
            this.denyItem = p -> item;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder denyItem(@NonNull Supplier<@Nullable ItemStack> supplier) {
            this.denyItem = p -> supplier.get();
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder denyItem(@NonNull Function<@NonNull Player, @Nullable ItemStack> function) {
            this.denyItem = function;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder border(@Nullable ItemStack item) {
            this.borderItem = item;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onConfirm(@Nullable Consumer<Player> onConfirm) {
            this.onConfirm = onConfirm;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onConfirm(@Nullable Runnable onConfirm) {
            this.onConfirm = onConfirm != null ? p -> onConfirm.run() : null;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onDeny(@Nullable Consumer<Player> onDeny) {
            this.onDeny = onDeny;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onDeny(@Nullable Runnable onDeny) {
            this.onDeny = onDeny != null ? p -> onDeny.run() : null;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder onClose(@Nullable Consumer<Player> onClose) {
            this.onClose = onClose;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder confirmSound(@Nullable Sound sound) {
            this.confirmSound = sound;
            return this;
        }

        @CheckReturnValue
        public @NonNull Builder denySound(@Nullable Sound sound) {
            this.denySound = sound;
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

        @Contract(" -> new")
        public @NonNull ConfirmMenu build() {
            return new ConfirmMenu(this);
        }

        public void open(@NonNull Player player) {
            build().open(player);
        }
    }
}
