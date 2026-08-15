package dev.oum.oumlib.entity.display;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class VirtualTextDisplay extends AbstractVirtualDisplay {

    private Component staticText;
    private Function<Player, Component> dynamicText;
    private int lineWidth = 200;
    private int backgroundColor = 0x40000000;
    private byte textOpacity = -1;
    private boolean shadow = false;
    private boolean seeThrough = false;
    private boolean defaultBackground = false;
    private Alignment alignment = Alignment.CENTER;

    public VirtualTextDisplay(@NonNull Location location, @NonNull Component text) {
        super(location);
        this.staticText = Objects.requireNonNull(text);
    }

    public VirtualTextDisplay(@NonNull Location location, @NonNull String miniMessage) {
        super(location);
        this.staticText = Text.parse(miniMessage);
    }

    public VirtualTextDisplay(@NonNull Location location, @NonNull Function<Player, Component> dynamicText) {
        super(location);
        this.dynamicText = Objects.requireNonNull(dynamicText);
    }

    public static @NonNull VirtualTextDisplay of(@NonNull Location location, @NonNull Component text) {
        return new VirtualTextDisplay(location, text);
    }

    public static @NonNull VirtualTextDisplay of(@NonNull Location location, @NonNull String miniMessage) {
        return new VirtualTextDisplay(location, miniMessage);
    }

    public static @NonNull VirtualTextDisplay dynamic(@NonNull Location location, @NonNull Function<Player, Component> dynamicText) {
        return new VirtualTextDisplay(location, dynamicText);
    }

    @Override
    protected @NonNull EntityType getEntityType() {
        return EntityTypes.TEXT_DISPLAY;
    }

    public void setText(@NonNull Component text) {
        this.staticText = Objects.requireNonNull(text);
        this.dynamicText = null;
    }

    public void setText(@NonNull String miniMessage) {
        this.staticText = Text.parse(miniMessage);
        this.dynamicText = null;
    }

    public void setText(@NonNull Function<Player, Component> dynamicText) {
        this.dynamicText = Objects.requireNonNull(dynamicText);
        this.staticText = null;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public byte getTextOpacity() {
        return textOpacity;
    }

    public void setTextOpacity(byte textOpacity) {
        this.textOpacity = textOpacity;
    }

    public boolean hasShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public boolean isSeeThrough() {
        return seeThrough;
    }

    public void setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
    }

    public boolean isDefaultBackground() {
        return defaultBackground;
    }

    public void setDefaultBackground(boolean defaultBackground) {
        this.defaultBackground = defaultBackground;
    }

    public @NonNull Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(@NonNull Alignment alignment) {
        this.alignment = alignment;
    }

    @Override
    protected void appendCustomMetadata(@NonNull Player player, @NonNull List<EntityData<?>> list) {
        Component text = dynamicText != null ? dynamicText.apply(player) : staticText;
        if (text == null) {
            text = Component.empty();
        }
        list.add(new EntityData<>(23, EntityDataTypes.ADV_COMPONENT, text));
        list.add(new EntityData<>(24, EntityDataTypes.INT, lineWidth));
        list.add(new EntityData<>(25, EntityDataTypes.INT, backgroundColor));
        list.add(new EntityData<>(26, EntityDataTypes.BYTE, textOpacity));

        byte flags = 0;
        if (shadow) flags |= 0x01;
        if (seeThrough) flags |= 0x02;
        if (defaultBackground) flags |= 0x04;
        if (alignment == Alignment.LEFT) flags |= 0x08;
        if (alignment == Alignment.RIGHT) flags |= 0x10;
        list.add(new EntityData<>(27, EntityDataTypes.BYTE, flags));
    }

    public enum Alignment {
        CENTER,
        LEFT,
        RIGHT
    }
}
