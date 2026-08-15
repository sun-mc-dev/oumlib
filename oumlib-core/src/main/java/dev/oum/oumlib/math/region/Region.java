package dev.oum.oumlib.math.region;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Region extends Iterable<Block> {

    @Nullable World getWorld();

    @NonNull String getWorldName();

    boolean contains(@NonNull Location location);

    boolean contains(@NonNull Vector vector);

    boolean contains(double x, double y, double z);

    boolean contains(@NonNull Block block);

    boolean overlaps(@NonNull Region other);

    @NonNull Location getCenter();

    double getVolume();

    long getBlockCount();

    @NonNull Location getRandomLocation();

    @NonNull Location getRandomLocation(@NonNull Random random);

    @NonNull Set<Chunk> getIntersectingChunks();

    @NonNull Set<Long> getIntersectingChunkKeys();

    @Override
    @NonNull Iterator<Block> iterator();

    default @NonNull Iterable<Block> getBlocks() {
        return this;
    }

    default @NonNull Stream<Block> streamBlocks() {
        return StreamSupport.stream(spliterator(), false);
    }

    default void forEachBlock(@NonNull Consumer<Block> action) {
        for (Block block : this) {
            action.accept(block);
        }
    }

    @NonNull Map<String, Object> serialize();
}
