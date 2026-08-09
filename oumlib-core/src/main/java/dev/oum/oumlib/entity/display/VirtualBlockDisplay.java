package dev.oum.oumlib.entity.display;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

public class VirtualBlockDisplay extends AbstractVirtualDisplay {

    private BlockData blockData;

    public VirtualBlockDisplay(@NonNull Location location, @NonNull BlockData blockData) {
        super(location);
        this.blockData = Objects.requireNonNull(blockData);
    }

    public VirtualBlockDisplay(@NonNull Location location, @NonNull Material material) {
        this(location, Bukkit.createBlockData(material));
    }

    public static @NonNull VirtualBlockDisplay of(@NonNull Location location, @NonNull BlockData blockData) {
        return new VirtualBlockDisplay(location, blockData);
    }

    public static @NonNull VirtualBlockDisplay of(@NonNull Location location, @NonNull Material material) {
        return new VirtualBlockDisplay(location, material);
    }

    @Override
    protected @NonNull EntityType getEntityType() {
        return EntityTypes.BLOCK_DISPLAY;
    }

    public @NonNull BlockData getBlockData() {
        return blockData;
    }

    public void setBlockData(@NonNull BlockData blockData) {
        this.blockData = Objects.requireNonNull(blockData);
    }

    @Override
    protected void appendCustomMetadata(@NonNull Player player, @NonNull List<EntityData<?>> list) {
        int blockStateId = SpigotConversionUtil.fromBukkitBlockData(blockData).getGlobalId();
        list.add(new EntityData<>(23, EntityDataTypes.INT, blockStateId));
    }
}
