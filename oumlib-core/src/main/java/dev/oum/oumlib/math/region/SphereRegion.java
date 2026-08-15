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

public class SphereRegion implements Region {

    private final String worldName;
    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final double radius;

    public SphereRegion(@NonNull Location center, double radius) {
        World w = center.getWorld();
        this.worldName = w != null ? w.getName() : "world";
        this.centerX = center.getX();
        this.centerY = center.getY();
        this.centerZ = center.getZ();
        this.radius = Math.max(0.0, radius);
    }

    public SphereRegion(@NonNull String worldName, double centerX, double centerY, double centerZ, double radius) {
        this.worldName = worldName;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = Math.max(0.0, radius);
    }

    public static @NonNull SphereRegion of(@NonNull Location center, double radius) {
        return new SphereRegion(center, radius);
    }

    public static @NonNull SphereRegion deserialize(@NonNull Map<String, Object> map) {
        String world = (String) map.getOrDefault("world", "world");
        double centerX = ((Number) map.get("centerX")).doubleValue();
        double centerY = ((Number) map.get("centerY")).doubleValue();
        double centerZ = ((Number) map.get("centerZ")).doubleValue();
        double radius = ((Number) map.get("radius")).doubleValue();
        return new SphereRegion(world, centerX, centerY, centerZ, radius);
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

    public double getCenterY() {
        return centerY;
    }

    public double getCenterZ() {
        return centerZ;
    }

    public double getRadius() {
        return radius;
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
        double dx = x - centerX;
        double dy = y - centerY;
        double dz = z - centerZ;
        return (dx * dx + dy * dy + dz * dz) <= (radius * radius);
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
        return new Location(getWorld(), centerX, centerY, centerZ);
    }

    @Override
    public double getVolume() {
        return (4.0 / 3.0) * Math.PI * radius * radius * radius;
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
        double u = random.nextDouble();
        double v = random.nextDouble();
        double theta = u * 2.0 * Math.PI;
        double phi = Math.acos(2.0 * v - 1.0);
        double r = Math.cbrt(random.nextDouble()) * radius;
        double sinPhi = Math.sin(phi);
        double rx = centerX + r * sinPhi * Math.cos(theta);
        double ry = centerY + r * Math.cos(phi);
        double rz = centerZ + r * sinPhi * Math.sin(theta);
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
            private final int minBy = (int) Math.floor(centerY - radius);
            private final int maxBy = (int) Math.floor(centerY + radius);
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
        map.put("type", "sphere");
        map.put("world", worldName);
        map.put("centerX", centerX);
        map.put("centerY", centerY);
        map.put("centerZ", centerZ);
        map.put("radius", radius);
        return map;
    }
}
