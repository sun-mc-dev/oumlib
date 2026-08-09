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

public class CylinderRegion implements Region {

    private final String worldName;
    private final double centerX;
    private final double centerZ;
    private final double radius;
    private final double minY;
    private final double maxY;

    public CylinderRegion(@NonNull Location center, double radius, double minY, double maxY) {
        World w = center.getWorld();
        this.worldName = w != null ? w.getName() : "world";
        this.centerX = center.getX();
        this.centerZ = center.getZ();
        this.radius = Math.max(0.0, radius);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
    }

    public CylinderRegion(@NonNull String worldName, double centerX, double centerZ, double radius, double minY, double maxY) {
        this.worldName = worldName;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = Math.max(0.0, radius);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
    }

    public static @NonNull CylinderRegion of(@NonNull Location center, double radius, double height) {
        double half = height / 2.0;
        return new CylinderRegion(center, radius, center.getY() - half, center.getY() + half);
    }

    public static @NonNull CylinderRegion deserialize(@NonNull Map<String, Object> map) {
        String world = (String) map.getOrDefault("world", "world");
        double centerX = ((Number) map.get("centerX")).doubleValue();
        double centerZ = ((Number) map.get("centerZ")).doubleValue();
        double radius = ((Number) map.get("radius")).doubleValue();
        double minY = ((Number) map.get("minY")).doubleValue();
        double maxY = ((Number) map.get("maxY")).doubleValue();
        return new CylinderRegion(world, centerX, centerZ, radius, minY, maxY);
    }

    @Override
    public @Nullable World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public @NonNull String getWorldName() {
        return worldName;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterZ() {
        return centerZ;
    }

    public double getRadius() {
        return radius;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    @Override
    public boolean contains(@NonNull Location location) {
        World w = location.getWorld();
        if (w != null && !w.getName().equalsIgnoreCase(worldName)) {
            return false;
        }
        return contains(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public boolean contains(@NonNull Vector vector) {
        return contains(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public boolean contains(double x, double y, double z) {
        if (y < minY || y > maxY) return false;
        double dx = x - centerX;
        double dz = z - centerZ;
        return (dx * dx + dz * dz) <= (radius * radius);
    }

    @Override
    public boolean contains(@NonNull Block block) {
        World w = block.getWorld();
        if (!w.getName().equalsIgnoreCase(worldName)) {
            return false;
        }
        return contains(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5);
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
        return new Location(getWorld(), centerX, minY + (maxY - minY) / 2.0, centerZ);
    }

    @Override
    public double getVolume() {
        return Math.PI * radius * radius * (maxY - minY);
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
        double r = radius * Math.sqrt(random.nextDouble());
        double theta = random.nextDouble() * 2 * Math.PI;
        double rx = centerX + r * Math.cos(theta);
        double rz = centerZ + r * Math.sin(theta);
        double ry = minY + (maxY - minY) * random.nextDouble();
        return new Location(getWorld(), rx, ry, rz);
    }

    @Override
    public @NonNull Set<Chunk> getIntersectingChunks() {
        World w = getWorld();
        if (w == null) return Collections.emptySet();
        Set<Chunk> chunks = new HashSet<>();
        int minChunkX = (int) Math.floor(centerX - radius) >> 4;
        int maxChunkX = (int) Math.floor(centerX + radius) >> 4;
        int minChunkZ = (int) Math.floor(centerZ - radius) >> 4;
        int maxChunkZ = (int) Math.floor(centerZ + radius) >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                chunks.add(w.getChunkAt(cx, cz));
            }
        }
        return chunks;
    }

    @Override
    public @NonNull Set<Long> getIntersectingChunkKeys() {
        Set<Long> keys = new HashSet<>();
        int minChunkX = (int) Math.floor(centerX - radius) >> 4;
        int maxChunkX = (int) Math.floor(centerX + radius) >> 4;
        int minChunkZ = (int) Math.floor(centerZ - radius) >> 4;
        int maxChunkZ = (int) Math.floor(centerZ + radius) >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                keys.add(Chunk.getChunkKey(cx, cz));
            }
        }
        return keys;
    }

    @Override
    public @NonNull Iterator<Block> iterator() {
        return new Iterator<>() {
            private final World world = getWorld();
            private final int minBx = (int) Math.floor(centerX - radius);
            private final int maxBx = (int) Math.floor(centerX + radius);
            private final int minBy = (int) Math.floor(minY);
            private final int maxBy = (int) Math.floor(maxY);
            private final int minBz = (int) Math.floor(centerZ - radius);
            private final int maxBz = (int) Math.floor(centerZ + radius);

            private int currentX = minBx;
            private int currentY = minBy;
            private int currentZ = minBz;
            private Block nextBlock = computeNext();

            private Block computeNext() {
                if (world == null) return null;
                while (currentY <= maxBy) {
                    while (currentZ <= maxBz) {
                        while (currentX <= maxBx) {
                            int bx = currentX++;
                            if (contains(bx + 0.5, currentY + 0.5, currentZ + 0.5)) {
                                return world.getBlockAt(bx, currentY, currentZ);
                            }
                        }
                        currentX = minBx;
                        currentZ++;
                    }
                    currentZ = minBz;
                    currentY++;
                }
                return null;
            }

            @Override
            public boolean hasNext() {
                return nextBlock != null;
            }

            @Override
            public Block next() {
                if (nextBlock == null) throw new NoSuchElementException();
                Block cur = nextBlock;
                nextBlock = computeNext();
                return cur;
            }
        };
    }

    @Override
    public @NonNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "cylinder");
        map.put("world", worldName);
        map.put("centerX", centerX);
        map.put("centerZ", centerZ);
        map.put("radius", radius);
        map.put("minY", minY);
        map.put("maxY", maxY);
        return map;
    }
}
