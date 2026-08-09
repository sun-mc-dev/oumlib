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

public class CuboidRegion implements Region {

    private final String worldName;
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;

    public CuboidRegion(@NonNull Location loc1, @NonNull Location loc2) {
        World w = loc1.getWorld() != null ? loc1.getWorld() : loc2.getWorld();
        this.worldName = w != null ? w.getName() : "world";
        this.minX = Math.min(loc1.getX(), loc2.getX());
        this.minY = Math.min(loc1.getY(), loc2.getY());
        this.minZ = Math.min(loc1.getZ(), loc2.getZ());
        this.maxX = Math.max(loc1.getX(), loc2.getX());
        this.maxY = Math.max(loc1.getY(), loc2.getY());
        this.maxZ = Math.max(loc1.getZ(), loc2.getZ());
    }

    public CuboidRegion(@NonNull String worldName, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.worldName = worldName;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public static @NonNull CuboidRegion of(@NonNull Location loc1, @NonNull Location loc2) {
        return new CuboidRegion(loc1, loc2);
    }

    public static @NonNull CuboidRegion centered(@NonNull Location center, double radiusX, double radiusY, double radiusZ) {
        World w = center.getWorld();
        String wName = w != null ? w.getName() : "world";
        return new CuboidRegion(wName,
                center.getX() - radiusX, center.getY() - radiusY, center.getZ() - radiusZ,
                center.getX() + radiusX, center.getY() + radiusY, center.getZ() + radiusZ);
    }

    public static @NonNull CuboidRegion deserialize(@NonNull Map<String, Object> map) {
        String world = (String) map.getOrDefault("world", "world");
        double minX = ((Number) map.get("minX")).doubleValue();
        double minY = ((Number) map.get("minY")).doubleValue();
        double minZ = ((Number) map.get("minZ")).doubleValue();
        double maxX = ((Number) map.get("maxX")).doubleValue();
        double maxY = ((Number) map.get("maxY")).doubleValue();
        double maxZ = ((Number) map.get("maxZ")).doubleValue();
        return new CuboidRegion(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public @Nullable World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public @NonNull String getWorldName() {
        return worldName;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public int getMinBlockX() {
        return (int) Math.floor(minX);
    }

    public int getMinBlockY() {
        return (int) Math.floor(minY);
    }

    public int getMinBlockZ() {
        return (int) Math.floor(minZ);
    }

    public int getMaxBlockX() {
        return (int) Math.floor(maxX);
    }

    public int getMaxBlockY() {
        return (int) Math.floor(maxY);
    }

    public int getMaxBlockZ() {
        return (int) Math.floor(maxZ);
    }

    public @NonNull Location getMinimumPoint() {
        return new Location(getWorld(), minX, minY, minZ);
    }

    public @NonNull Location getMaximumPoint() {
        return new Location(getWorld(), maxX, maxY, maxZ);
    }

    public double getWidthX() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }

    public double getLengthZ() {
        return maxZ - minZ;
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
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    @Override
    public boolean contains(@NonNull Block block) {
        World w = block.getWorld();
        if (!w.getName().equalsIgnoreCase(worldName)) {
            return false;
        }
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        return bx >= getMinBlockX() && bx <= getMaxBlockX()
                && by >= getMinBlockY() && by <= getMaxBlockY()
                && bz >= getMinBlockZ() && bz <= getMaxBlockZ();
    }

    @Override
    public boolean overlaps(@NonNull Region other) {
        if (!worldName.equalsIgnoreCase(other.getWorldName())) {
            return false;
        }
        if (other instanceof CuboidRegion c) {
            return this.minX <= c.maxX && this.maxX >= c.minX
                    && this.minY <= c.maxY && this.maxY >= c.minY
                    && this.minZ <= c.maxZ && this.maxZ >= c.minZ;
        }
        for (Block b : other) {
            if (contains(b)) return true;
        }
        return false;
    }

    @Override
    public @NonNull Location getCenter() {
        return new Location(getWorld(),
                minX + (maxX - minX) / 2.0,
                minY + (maxY - minY) / 2.0,
                minZ + (maxZ - minZ) / 2.0);
    }

    @Override
    public double getVolume() {
        return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }

    @Override
    public long getBlockCount() {
        long dx = (long) getMaxBlockX() - getMinBlockX() + 1;
        long dy = (long) getMaxBlockY() - getMinBlockY() + 1;
        long dz = (long) getMaxBlockZ() - getMinBlockZ() + 1;
        return dx * dy * dz;
    }

    @Override
    public @NonNull Location getRandomLocation() {
        return getRandomLocation(new Random());
    }

    @Override
    public @NonNull Location getRandomLocation(@NonNull Random random) {
        double rx = minX + (maxX - minX) * random.nextDouble();
        double ry = minY + (maxY - minY) * random.nextDouble();
        double rz = minZ + (maxZ - minZ) * random.nextDouble();
        return new Location(getWorld(), rx, ry, rz);
    }

    @Override
    public @NonNull Set<Chunk> getIntersectingChunks() {
        World w = getWorld();
        if (w == null) return Collections.emptySet();
        Set<Chunk> chunks = new HashSet<>();
        int minChunkX = getMinBlockX() >> 4;
        int maxChunkX = getMaxBlockX() >> 4;
        int minChunkZ = getMinBlockZ() >> 4;
        int maxChunkZ = getMaxBlockZ() >> 4;
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
        int minChunkX = getMinBlockX() >> 4;
        int maxChunkX = getMaxBlockX() >> 4;
        int minChunkZ = getMinBlockZ() >> 4;
        int maxChunkZ = getMaxBlockZ() >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                keys.add(Chunk.getChunkKey(cx, cz));
            }
        }
        return keys;
    }

    public @NonNull CuboidRegion expand(double amount) {
        return expand(amount, amount, amount);
    }

    public @NonNull CuboidRegion expand(double dx, double dy, double dz) {
        return new CuboidRegion(worldName, minX - dx, minY - dy, minZ - dz, maxX + dx, maxY + dy, maxZ + dz);
    }

    public @NonNull CuboidRegion contract(double amount) {
        return expand(-amount, -amount, -amount);
    }

    public @NonNull List<Location> getCorners() {
        World w = getWorld();
        return List.of(
                new Location(w, minX, minY, minZ),
                new Location(w, maxX, minY, minZ),
                new Location(w, minX, minY, maxZ),
                new Location(w, maxX, minY, maxZ),
                new Location(w, minX, maxY, minZ),
                new Location(w, maxX, maxY, minZ),
                new Location(w, minX, maxY, maxZ),
                new Location(w, maxX, maxY, maxZ)
        );
    }

    @Override
    public @NonNull Iterator<Block> iterator() {
        return new Iterator<>() {
            private final World world = getWorld();
            private final int minBx = getMinBlockX();
            private final int minBy = getMinBlockY();
            private final int minBz = getMinBlockZ();
            private final int maxBx = getMaxBlockX();
            private final int maxBy = getMaxBlockY();
            private final int maxBz = getMaxBlockZ();

            private int currentX = minBx;
            private int currentY = minBy;
            private int currentZ = minBz;

            @Override
            public boolean hasNext() {
                return world != null && currentX <= maxBx && currentY <= maxBy && currentZ <= maxBz;
            }

            @Override
            public Block next() {
                if (!hasNext()) throw new NoSuchElementException();
                Block block = world.getBlockAt(currentX, currentY, currentZ);
                currentX++;
                if (currentX > maxBx) {
                    currentX = minBx;
                    currentZ++;
                    if (currentZ > maxBz) {
                        currentZ = minBz;
                        currentY++;
                    }
                }
                return block;
            }
        };
    }

    @Override
    public @NonNull Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "cuboid");
        map.put("world", worldName);
        map.put("minX", minX);
        map.put("minY", minY);
        map.put("minZ", minZ);
        map.put("maxX", maxX);
        map.put("maxY", maxY);
        map.put("maxZ", maxZ);
        return map;
    }
}
