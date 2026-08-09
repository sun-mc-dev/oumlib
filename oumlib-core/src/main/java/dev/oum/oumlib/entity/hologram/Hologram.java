package dev.oum.oumlib.entity.hologram;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.entity.display.VirtualBlockDisplay;
import dev.oum.oumlib.entity.display.VirtualDisplay;
import dev.oum.oumlib.entity.display.VirtualItemDisplay;
import dev.oum.oumlib.entity.display.VirtualTextDisplay;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

public class Hologram {

    private final UUID id = UUID.randomUUID();
    private final List<HologramLine> lines = new CopyOnWriteArrayList<>();
    private Location location;
    private double lineSpacing = 0.28;
    private double viewDistance = 48.0;
    private VirtualDisplay.Billboard billboard = VirtualDisplay.Billboard.CENTER;
    private Predicate<Player> visibilityFilter;
    private ClickListener clickListener;

    public Hologram(@NonNull Location location) {
        this.location = location.clone();
    }

    public static @NonNull Builder builder(@NonNull Location location) {
        return new Builder(location);
    }

    public @NonNull UUID getId() {
        return id;
    }

    public @NonNull Location getLocation() {
        return location.clone();
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(double lineSpacing) {
        this.lineSpacing = lineSpacing;
        realignLines();
    }

    public double getViewDistance() {
        return viewDistance;
    }

    public void setViewDistance(double viewDistance) {
        this.viewDistance = viewDistance;
        for (HologramLine line : lines) {
            line.getDisplay().setViewDistance(viewDistance);
        }
    }

    public VirtualDisplay.@NonNull Billboard getBillboard() {
        return billboard;
    }

    public void setBillboard(VirtualDisplay.@NonNull Billboard billboard) {
        this.billboard = billboard;
        for (HologramLine line : lines) {
            line.getDisplay().setBillboard(billboard);
        }
    }

    public void setVisibilityFilter(@Nullable Predicate<Player> filter) {
        this.visibilityFilter = filter;
        for (HologramLine line : lines) {
            line.getDisplay().setVisibilityFilter(filter);
        }
    }

    public @Nullable ClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(@Nullable ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public int size() {
        return lines.size();
    }

    public @NonNull List<HologramLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public @Nullable HologramLine getLine(int index) {
        if (index < 0 || index >= lines.size()) return null;
        return lines.get(index);
    }

    public @NonNull Hologram addLine(@NonNull Component text) {
        VirtualTextDisplay display = new VirtualTextDisplay(location, text);
        return addDisplay(display, lineSpacing);
    }

    public @NonNull Hologram addLine(@NonNull String miniMessage) {
        VirtualTextDisplay display = new VirtualTextDisplay(location, miniMessage);
        return addDisplay(display, lineSpacing);
    }

    public @NonNull Hologram addDynamicLine(@NonNull Function<Player, Component> dynamicText) {
        VirtualTextDisplay display = new VirtualTextDisplay(location, dynamicText);
        return addDisplay(display, lineSpacing);
    }

    public @NonNull Hologram addItemLine(@NonNull ItemStack item) {
        VirtualItemDisplay display = new VirtualItemDisplay(location, item);
        return addDisplay(display, 0.45);
    }

    public @NonNull Hologram addItemLine(@NonNull Material material) {
        return addItemLine(new ItemStack(material));
    }

    public @NonNull Hologram addBlockLine(@NonNull BlockData blockData) {
        VirtualBlockDisplay display = new VirtualBlockDisplay(location, blockData);
        return addDisplay(display, 0.45);
    }

    public @NonNull Hologram addBlockLine(@NonNull Material material) {
        return addBlockLine(Bukkit.createBlockData(material));
    }

    public @NonNull Hologram addDisplay(@NonNull VirtualDisplay display, double heightOffset) {
        display.setViewDistance(viewDistance);
        display.setBillboard(billboard);
        display.setVisibilityFilter(visibilityFilter);

        SimpleHologramLine line = new SimpleHologramLine(display, heightOffset);
        lines.add(line);
        realignLines();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (display.isVisibleTo(p)) {
                display.spawn(p);
            }
        }
        return this;
    }

    public void removeLine(int index) {
        if (index < 0 || index >= lines.size()) return;
        HologramLine line = lines.remove(index);
        for (Player p : Bukkit.getOnlinePlayers()) {
            line.getDisplay().destroy(p);
        }
        realignLines();
    }

    public void clearLines() {
        for (HologramLine line : lines) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                line.getDisplay().destroy(p);
            }
        }
        lines.clear();
    }

    public void setLine(int index, @NonNull Component text) {
        if (index < 0 || index >= lines.size()) return;
        HologramLine line = lines.get(index);
        if (line.getDisplay() instanceof VirtualTextDisplay td) {
            td.setText(text);
            td.updateMetadata();
        }
    }

    public void setLine(int index, @NonNull String miniMessage) {
        setLine(index, Text.parse(miniMessage));
    }

    public void setLine(int index, @NonNull Function<Player, Component> dynamicText) {
        if (index < 0 || index >= lines.size()) return;
        HologramLine line = lines.get(index);
        if (line.getDisplay() instanceof VirtualTextDisplay td) {
            td.setText(dynamicText);
            td.updateMetadata();
        }
    }

    public void teleport(@NonNull Location location) {
        this.location = location.clone();
        realignLines();
    }

    public void update() {
        for (HologramLine line : lines) {
            line.getDisplay().updateMetadata();
        }
    }

    public void update(@NonNull Player player) {
        for (HologramLine line : lines) {
            line.getDisplay().updateMetadata(player);
        }
    }

    public void spawn(@NonNull Player player) {
        for (HologramLine line : lines) {
            line.getDisplay().spawn(player);
        }
    }

    public void destroy(@NonNull Player player) {
        for (HologramLine line : lines) {
            line.getDisplay().destroy(player);
        }
    }

    public void spawnAll() {
        for (HologramLine line : lines) {
            line.getDisplay().spawnAll(Bukkit.getOnlinePlayers());
        }
    }

    public void destroyAll() {
        for (HologramLine line : lines) {
            line.getDisplay().destroyAll(Bukkit.getOnlinePlayers());
        }
    }

    public void realignLines() {
        double currentY = location.getY();
        for (HologramLine line : lines) {
            Location lineLoc = new Location(location.getWorld(), location.getX(), currentY, location.getZ(), location.getYaw(), location.getPitch());
            line.teleport(lineLoc);
            currentY -= line.getHeightOffset();
        }
    }

    public int getLineIndexByEntityId(int entityId) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getDisplay().getEntityId() == entityId) {
                return i;
            }
        }
        return -1;
    }

    public void register() {
        OumLib.holograms().register(this);
    }

    public void unregister() {
        OumLib.holograms().unregister(this);
    }

    public enum ClickType {
        LEFT_CLICK,
        RIGHT_CLICK
    }

    @FunctionalInterface
    public interface ClickListener {
        void onClick(@NonNull Player player, @NonNull Hologram hologram, int lineIndex, @NonNull ClickType clickType);
    }

    private static final class SimpleHologramLine implements HologramLine {
        private final VirtualDisplay display;
        private final double heightOffset;

        private SimpleHologramLine(VirtualDisplay display, double heightOffset) {
            this.display = display;
            this.heightOffset = heightOffset;
        }

        @Override
        public @NonNull VirtualDisplay getDisplay() {
            return display;
        }

        @Override
        public double getHeightOffset() {
            return heightOffset;
        }

        @Override
        public void setLocation(@NonNull Location location) {
            display.setLocation(location);
        }

        @Override
        public void teleport(@NonNull Location location) {
            display.teleport(location);
        }
    }

    public static final class Builder {
        private final Location location;
        private final List<ConsumerAction> lineActions = new ArrayList<>();
        private double lineSpacing = 0.28;
        private double viewDistance = 48.0;
        private VirtualDisplay.Billboard billboard = VirtualDisplay.Billboard.CENTER;
        private Predicate<Player> visibilityFilter;
        private ClickListener clickListener;

        private Builder(Location location) {
            this.location = location;
        }

        public @NonNull Builder lineSpacing(double spacing) {
            this.lineSpacing = spacing;
            return this;
        }

        public @NonNull Builder viewDistance(double distance) {
            this.viewDistance = distance;
            return this;
        }

        public @NonNull Builder billboard(VirtualDisplay.@NonNull Billboard billboard) {
            this.billboard = billboard;
            return this;
        }

        public @NonNull Builder visibilityFilter(@Nullable Predicate<Player> filter) {
            this.visibilityFilter = filter;
            return this;
        }

        public @NonNull Builder onClick(@Nullable ClickListener listener) {
            this.clickListener = listener;
            return this;
        }

        public @NonNull Builder line(@NonNull Component text) {
            lineActions.add(h -> h.addLine(text));
            return this;
        }

        public @NonNull Builder line(@NonNull String miniMessage) {
            lineActions.add(h -> h.addLine(miniMessage));
            return this;
        }

        public @NonNull Builder dynamicLine(@NonNull Function<Player, Component> dynamicText) {
            lineActions.add(h -> h.addDynamicLine(dynamicText));
            return this;
        }

        public @NonNull Builder itemLine(@NonNull ItemStack item) {
            lineActions.add(h -> h.addItemLine(item));
            return this;
        }

        public @NonNull Builder itemLine(@NonNull Material material) {
            lineActions.add(h -> h.addItemLine(material));
            return this;
        }

        public @NonNull Builder blockLine(@NonNull BlockData blockData) {
            lineActions.add(h -> h.addBlockLine(blockData));
            return this;
        }

        public @NonNull Builder blockLine(@NonNull Material material) {
            lineActions.add(h -> h.addBlockLine(material));
            return this;
        }

        public @NonNull Hologram build() {
            Hologram h = new Hologram(location);
            h.setLineSpacing(lineSpacing);
            h.setViewDistance(viewDistance);
            h.setBillboard(billboard);
            h.setVisibilityFilter(visibilityFilter);
            h.setClickListener(clickListener);
            for (ConsumerAction action : lineActions) {
                action.accept(h);
            }
            return h;
        }

        public @NonNull Hologram buildAndRegister() {
            Hologram h = build();
            h.register();
            return h;
        }

        @FunctionalInterface
        private interface ConsumerAction {
            void accept(Hologram hologram);
        }
    }
}
