package dev.oum.oumlib.entity.display;

import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class DisplayBuilder<D extends Display, B extends DisplayBuilder<D, B>> {

    protected final Location location;
    protected final EntityType type;
    protected Display.Billboard billboard;
    protected Vector3f scale;
    protected Vector3f translation;
    protected Quaternionf leftRotation;
    protected Quaternionf rightRotation;
    protected Float shadowRadius;
    protected Float shadowStrength;
    protected Float viewRange;
    protected Color glowColor;
    protected Integer interpolationDuration;
    protected Integer teleportDuration;
    protected Boolean glowing;

    protected DisplayBuilder(Location location, EntityType type) {
        this.location = location;
        this.type = type;
    }

    @Contract("_, _ -> new")
    public static @NonNull TextDisplayBuilder text(@NonNull Location location, @NonNull Component text) {
        return new TextDisplayBuilder(location, text);
    }

    @Contract("_, _ -> new")
    public static @NonNull TextDisplayBuilder text(@NonNull Location location, @NonNull String miniMessage) {
        return new TextDisplayBuilder(location, Text.parse(miniMessage));
    }

    @Contract("_, _ -> new")
    public static @NonNull BlockDisplayBuilder block(@NonNull Location location, @NonNull BlockData blockData) {
        return new BlockDisplayBuilder(location, blockData);
    }

    @Contract("_, _ -> new")
    public static @NonNull ItemDisplayBuilder item(@NonNull Location location, @NonNull ItemStack itemStack) {
        return new ItemDisplayBuilder(location, itemStack);
    }

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    public B billboard(Display.@Nullable Billboard billboard) {
        this.billboard = billboard;
        return self();
    }

    public B scale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
        return self();
    }

    public B translation(float x, float y, float z) {
        this.translation = new Vector3f(x, y, z);
        return self();
    }

    public B leftRotation(float x, float y, float z, float w) {
        this.leftRotation = new Quaternionf(x, y, z, w);
        return self();
    }

    public B rightRotation(float x, float y, float z, float w) {
        this.rightRotation = new Quaternionf(x, y, z, w);
        return self();
    }

    public B shadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
        return self();
    }

    public B shadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
        return self();
    }

    public B viewRange(float viewRange) {
        this.viewRange = viewRange;
        return self();
    }

    public B glowColor(@Nullable Color glowColor) {
        this.glowColor = glowColor;
        return self();
    }

    public B glowing(boolean glowing) {
        this.glowing = glowing;
        return self();
    }

    public B interpolationDuration(int ticks) {
        this.interpolationDuration = ticks;
        return self();
    }

    public B teleportDuration(int ticks) {
        this.teleportDuration = ticks;
        return self();
    }

    protected void applyProperties(D display) {
        if (billboard != null) {
            display.setBillboard(billboard);
        }
        if (shadowRadius != null) {
            display.setShadowRadius(shadowRadius);
        }
        if (shadowStrength != null) {
            display.setShadowStrength(shadowStrength);
        }
        if (viewRange != null) {
            display.setViewRange(viewRange);
        }
        if (glowColor != null) {
            display.setGlowColorOverride(glowColor);
        }
        if (glowing != null) {
            display.setGlowing(glowing);
        }
        if (interpolationDuration != null) {
            display.setInterpolationDuration(interpolationDuration);
        }
        if (teleportDuration != null) {
            display.setTeleportDuration(teleportDuration);
        }
        if (scale != null || translation != null || leftRotation != null || rightRotation != null) {
            Vector3f t = translation != null ? translation : new Vector3f();
            Quaternionf lr = leftRotation != null ? leftRotation : new Quaternionf();
            Vector3f s = scale != null ? scale : new Vector3f(1, 1, 1);
            Quaternionf rr = rightRotation != null ? rightRotation : new Quaternionf();
            display.setTransformation(new Transformation(t, lr, s, rr));
        }
    }

    @SuppressWarnings("unchecked")
    public D spawn() {
        if (location.getWorld() == null) {
            throw new IllegalStateException("World is null");
        }
        D display = (D) location.getWorld().spawnEntity(location, type);
        applyProperties(display);
        return display;
    }

    public static final class TextDisplayBuilder extends DisplayBuilder<TextDisplay, TextDisplayBuilder> {
        private final Component text;
        private Color backgroundColor;
        private Integer lineWidth;
        private Byte opacity;
        private TextDisplay.TextAlignment alignment;
        private Boolean seeThrough;
        private Boolean shadow;

        private TextDisplayBuilder(Location location, Component text) {
            super(location, EntityType.TEXT_DISPLAY);
            this.text = text;
        }

        public TextDisplayBuilder backgroundColor(@Nullable Color color) {
            this.backgroundColor = color;
            return this;
        }

        public TextDisplayBuilder lineWidth(int width) {
            this.lineWidth = width;
            return this;
        }

        public TextDisplayBuilder opacity(byte opacity) {
            this.opacity = opacity;
            return this;
        }

        public TextDisplayBuilder alignment(TextDisplay.@Nullable TextAlignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public TextDisplayBuilder seeThrough(boolean seeThrough) {
            this.seeThrough = seeThrough;
            return this;
        }

        public TextDisplayBuilder shadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }

        @Override
        public @NonNull TextDisplay spawn() {
            TextDisplay display = super.spawn();
            display.text(text);
            if (backgroundColor != null) {
                display.setBackgroundColor(backgroundColor);
            }
            if (lineWidth != null) {
                display.setLineWidth(lineWidth);
            }
            if (opacity != null) {
                display.setTextOpacity(opacity);
            }
            if (alignment != null) {
                display.setAlignment(alignment);
            }
            if (seeThrough != null) {
                display.setSeeThrough(seeThrough);
            }
            if (shadow != null) {
                display.setShadowed(shadow);
            }
            return display;
        }
    }

    public static final class BlockDisplayBuilder extends DisplayBuilder<BlockDisplay, BlockDisplayBuilder> {
        private final BlockData blockData;

        private BlockDisplayBuilder(Location location, BlockData blockData) {
            super(location, EntityType.BLOCK_DISPLAY);
            this.blockData = blockData;
        }

        @Override
        public @NonNull BlockDisplay spawn() {
            BlockDisplay display = super.spawn();
            display.setBlock(blockData);
            return display;
        }
    }

    public static final class ItemDisplayBuilder extends DisplayBuilder<ItemDisplay, ItemDisplayBuilder> {
        private final ItemStack itemStack;

        private ItemDisplayBuilder(Location location, ItemStack itemStack) {
            super(location, EntityType.ITEM_DISPLAY);
            this.itemStack = itemStack;
        }

        @Override
        public @NonNull ItemDisplay spawn() {
            ItemDisplay display = super.spawn();
            display.setItemStack(itemStack);
            return display;
        }
    }
}
