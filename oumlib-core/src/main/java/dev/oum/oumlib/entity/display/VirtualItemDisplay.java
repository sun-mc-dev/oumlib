package dev.oum.oumlib.entity.display;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public class VirtualItemDisplay extends AbstractVirtualDisplay {

    private ItemStack itemStack;
    private Transform transform = Transform.FIXED;

    public VirtualItemDisplay(@NonNull Location location, @NonNull ItemStack itemStack) {
        super(location);
        this.itemStack = Objects.requireNonNull(itemStack).clone();
    }

    public VirtualItemDisplay(@NonNull Location location, @NonNull Material material) {
        this(location, new ItemStack(material));
    }

    public static @NonNull VirtualItemDisplay of(@NonNull Location location, @NonNull ItemStack itemStack) {
        return new VirtualItemDisplay(location, itemStack);
    }

    public static @NonNull VirtualItemDisplay of(@NonNull Location location, @NonNull Material material) {
        return new VirtualItemDisplay(location, material);
    }

    @Override
    protected @NonNull EntityType getEntityType() {
        return EntityTypes.ITEM_DISPLAY;
    }

    public @NonNull ItemStack getItemStack() {
        return itemStack.clone();
    }

    public void setItemStack(@NonNull ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack).clone();
    }

    public @NonNull Transform getTransform() {
        return transform;
    }

    public void setTransform(@NonNull Transform transform) {
        this.transform = Objects.requireNonNull(transform);
    }

    @Override
    protected void appendCustomMetadata(@NonNull Player player, @NonNull List<EntityData<?>> list) {
        com.github.retrooper.packetevents.protocol.item.ItemStack peItem =
                SpigotConversionUtil.fromBukkitItemStack(itemStack);
        list.add(new EntityData<>(23, EntityDataTypes.ITEMSTACK, peItem));
        list.add(new EntityData<>(24, EntityDataTypes.BYTE, transform.getId()));
    }

    public enum Transform {
        NONE((byte) 0),
        THIRDPERSON_LEFTHAND((byte) 1),
        THIRDPERSON_RIGHTHAND((byte) 2),
        FIRSTPERSON_LEFTHAND((byte) 3),
        FIRSTPERSON_RIGHTHAND((byte) 4),
        HEAD((byte) 5),
        GUI((byte) 6),
        GROUND((byte) 7),
        FIXED((byte) 8);

        private final byte id;

        Transform(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }
    }
}
