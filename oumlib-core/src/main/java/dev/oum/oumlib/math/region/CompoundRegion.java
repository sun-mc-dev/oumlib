package dev.oum.oumlib.math.region;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class CompoundRegion implements Region {

    private final String worldName;
    private final Mode mode;
    private final List<Region> children;

    public CompoundRegion(@NonNull Mode mode, @NonNull List<Region> children) {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Compound region must contain at least one child region.");
        }
        this.mode = mode;
        this.children = List.copyOf(children);
        this.worldName = children.getFirst().getWorldName();
        for (Region r : children) {
            if (!r.getWorldName().equalsIgnoreCase(this.worldName)) {
                throw new IllegalArgumentException("All child regions must belong to the same world.");
            }
        }
    }

    public static @NonNull CompoundRegion union(@NonNull Region... regions) {
        return new CompoundRegion(Mode.UNION, List.of(regions));
    }

    public static @NonNull CompoundRegion intersection(@NonNull Region... regions) {
        return new CompoundRegion(Mode.INTERSECTION, List.of(regions));
    }

    public @NonNull Mode getMode() {
        return mode;
    }

    public @NonNull List<Region> getChildren() {
        return children;
    }

    @Override
    public @Nullable World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public @NonNull String getWorldName() {
        return worldName;
    }

    @Override
    public boolean contains(@NonNull Location location) {
        World w = location.getWorld();
        if (w != null && !w.getName().equalsIgnoreCase(worldName)) {
            return false;
        }
        if (mode == Mode.UNION) {
            for (Region r : children) {
                if (r.contains(location)) return true;
            }
            return false;
        } else {
            for (Region r : children) {
                if (!r.contains(location)) return false;
            }
            return true;
        }
    }

    @Override
    public boolean contains(@NonNull Vector vector) {
        if (mode == Mode.UNION) {
            for (Region r : children) {
                if (r.contains(vector)) return true;
            }
            return false;
        } else {
            for (Region r : children) {
                if (!r.contains(vector)) return false;
            }
            return true;
        }
    }

    @Override
    public boolean contains(double x, double y, double z) {
        if (mode == Mode.UNION) {
            for (Region r : children) {
                if (r.contains(x, y, z)) return true;
            }
            return false;
        } else {
            for (Region r : children) {
                if (!r.contains(x, y, z)) return false;
            }
            return true;
        }
    }

    @Override
    public boolean contains(@NonNull Block block) {
        if (mode == Mode.UNION) {
            for (Region r : children) {
                if (r.contains(block)) return true;
            }
            return false;
        } else {
            for (Region r : children) {
                if (!r.contains(block)) return false;
            }
            return true;
        }
    }

    @Override
    public boolean overlaps(@NonNull Region other) {
        if (!worldName.equalsIgnoreCase(other.getWorldName())) return false;
        for (Block b : other) {
            if (contains(b)) return true;
        }
        return false;
    }

    @Override
    public @NonNull Location getCenter() {
        return children.getFirst().getCenter();
    }

    @Override
    public double getVolume() {
        double sum = 0;
        for (Region r : children) {
            sum += r.getVolume();
        }
        return sum;
    }

    @Override
    public long getBlockCount() {
        long count = 0;
        for (Block ignored : this) {
            count++;
        }
        return count;
    }

    @Override
    public @NonNull Location getRandomLocation() {
        return getRandomLocation(new Random());
    }

    @Override
    public @NonNull Location getRandomLocation(@NonNull Random random) {
        if (children.isEmpty()) return new Location(getWorld(), 0, 0, 0);
        Region picked = children.get(random.nextInt(children.size()));
        return picked.getRandomLocation(random);
    }

    @Override
    public @NonNull Set<Chunk> getIntersectingChunks() {
        Set<Chunk> chunks = new HashSet<>();
        for (Region r : children) {
            chunks.addAll(r.getIntersectingChunks());
        }
        return chunks;
    }

    @Override
    public @NonNull Set<Long> getIntersectingChunkKeys() {
        Set<Long> keys = new HashSet<>();
        for (Region r : children) {
            keys.addAll(r.getIntersectingChunkKeys());
        }
        return keys;
    }

    @Override
    public @NonNull Iterator<Block> iterator() {
        Set<Block> uniqueBlocks = new LinkedHashSet<>();
        if (mode == Mode.UNION) {
            for (Region r : children) {
                for (Block b : r) {
                    uniqueBlocks.add(b);
                }
            }
        } else {
            if (!children.isEmpty()) {
                for (Block b : children.getFirst()) {
                    boolean insideAll = true;
                    for (int i = 1; i < children.size(); i++) {
                        if (!children.get(i).contains(b)) {
                            insideAll = false;
                            break;
                        }
                    }
                    if (insideAll) {
                        uniqueBlocks.add(b);
                    }
                }
            }
        }
        return uniqueBlocks.iterator();
    }

    @Override
    public @NonNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "compound");
        map.put("mode", mode.name().toLowerCase(Locale.ROOT));
        List<Map<String, Object>> childList = new ArrayList<>();
        for (Region r : children) {
            childList.add(r.serialize());
        }
        map.put("children", childList);
        return map;
    }

    public enum Mode {
        UNION,
        INTERSECTION
    }
}
