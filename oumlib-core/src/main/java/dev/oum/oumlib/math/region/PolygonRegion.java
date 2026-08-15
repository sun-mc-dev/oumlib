package dev.oum.oumlib.math.region;

import dev.oum.oumlib.math.Vector2D;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class PolygonRegion implements Region {

    private final String worldName;
    private final List<Vector2D> points;
    private final double minY;
    private final double maxY;
    private final double minX;
    private final double maxX;
    private final double minZ;
    private final double maxZ;

    public PolygonRegion(@NonNull String worldName, @NonNull List<Vector2D> points, double minY, double maxY) {
        if (points.size() < 3) {
            throw new IllegalArgumentException("A polygon region must have at least 3 points.");
        }
        this.worldName = worldName;
        this.points = List.copyOf(points);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);

        double calcMinX = Double.POSITIVE_INFINITY;
        double calcMaxX = Double.NEGATIVE_INFINITY;
        double calcMinZ = Double.POSITIVE_INFINITY;
        double calcMaxZ = Double.NEGATIVE_INFINITY;

        for (Vector2D pt : points) {
            calcMinX = Math.min(calcMinX, pt.x());
            calcMaxX = Math.max(calcMaxX, pt.x());
            calcMinZ = Math.min(calcMinZ, pt.z());
            calcMaxZ = Math.max(calcMaxZ, pt.z());
        }

        this.minX = calcMinX;
        this.maxX = calcMaxX;
        this.minZ = calcMinZ;
        this.maxZ = calcMaxZ;
    }

    public static @NonNull PolygonRegion of(@NonNull String worldName, @NonNull List<Vector2D> points, double minY, double maxY) {
        return new PolygonRegion(worldName, points, minY, maxY);
    }

    @SuppressWarnings("unchecked")
    public static @NonNull PolygonRegion deserialize(@NonNull Map<String, Object> map) {
        String world = (String) map.getOrDefault("world", "world");
        double minY = ((Number) map.get("minY")).doubleValue();
        double maxY = ((Number) map.get("maxY")).doubleValue();
        List<Map<String, Number>> ptList = (List<Map<String, Number>>) map.get("points");
        List<Vector2D> points = new ArrayList<>();
        if (ptList != null) {
            for (Map<String, Number> p : ptList) {
                points.add(Vector2D.of(p.get("x").doubleValue(), p.get("z").doubleValue()));
            }
        }
        return new PolygonRegion(world, points, minY, maxY);
    }

    @Override
    public @Nullable World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Override
    public @NonNull String getWorldName() {
        return worldName;
    }

    public @NonNull List<Vector2D> getPoints() {
        return points;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
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
        if (x < minX || x > maxX || z < minZ || z > maxZ) return false;

        boolean inside = false;
        int n = points.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Vector2D pi = points.get(i);
            Vector2D pj = points.get(j);

            boolean intersect = ((pi.z() > z) != (pj.z() > z))
                    && (x < (pj.x() - pi.x()) * (z - pi.z()) / (pj.z() - pi.z()) + pi.x());
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
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
        double sumX = 0;
        double sumZ = 0;
        for (Vector2D pt : points) {
            sumX += pt.x();
            sumZ += pt.z();
        }
        return new Location(getWorld(), sumX / points.size(), minY + (maxY - minY) / 2.0, sumZ / points.size());
    }

    @Override
    public double getVolume() {
        double area = 0.0;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            Vector2D p1 = points.get(i);
            Vector2D p2 = points.get((i + 1) % n);
            area += (p1.x() * p2.z()) - (p2.x() * p1.z());
        }
        area = Math.abs(area) / 2.0;
        return area * (maxY - minY);
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
        for (int attempts = 0; attempts < 1000; attempts++) {
            double rx = minX + (maxX - minX) * random.nextDouble();
            double rz = minZ + (maxZ - minZ) * random.nextDouble();
            double ry = minY + (maxY - minY) * random.nextDouble();
            if (contains(rx, ry, rz)) {
                return new Location(getWorld(), rx, ry, rz);
            }
        }
        return getCenter();
    }

    @Override
    public @NonNull Set<Chunk> getIntersectingChunks() {
        World w = getWorld();
        if (w == null) return Collections.emptySet();
        Set<Chunk> chunks = new HashSet<>();
        int minChunkX = (int) Math.floor(minX) >> 4;
        int maxChunkX = (int) Math.floor(maxX) >> 4;
        int minChunkZ = (int) Math.floor(minZ) >> 4;
        int maxChunkZ = (int) Math.floor(maxZ) >> 4;
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
        int minChunkX = (int) Math.floor(minX) >> 4;
        int maxChunkX = (int) Math.floor(maxX) >> 4;
        int minChunkZ = (int) Math.floor(minZ) >> 4;
        int maxChunkZ = (int) Math.floor(maxZ) >> 4;
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
            private final int minBx = (int) Math.floor(minX);
            private final int maxBx = (int) Math.floor(maxX);
            private final int minBy = (int) Math.floor(minY);
            private final int maxBy = (int) Math.floor(maxY);
            private final int minBz = (int) Math.floor(minZ);
            private final int maxBz = (int) Math.floor(maxZ);

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
        map.put("type", "polygon");
        map.put("world", worldName);
        map.put("minY", minY);
        map.put("maxY", maxY);
        List<Map<String, Double>> ptList = new ArrayList<>();
        for (Vector2D pt : points) {
            ptList.add(Map.of("x", pt.x(), "z", pt.z()));
        }
        map.put("points", ptList);
        return map;
    }
}
