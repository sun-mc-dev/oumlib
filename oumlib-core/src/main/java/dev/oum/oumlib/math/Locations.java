package dev.oum.oumlib.math;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class Locations {

    private Locations() {
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull String serialize(@NonNull Location loc) {
        return (loc.getWorld() != null ? loc.getWorld().getName() : "world") + "," +
                loc.getX() + "," +
                loc.getY() + "," +
                loc.getZ() + "," +
                loc.getYaw() + "," +
                loc.getPitch();
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull String serializeBlock(@NonNull Location loc) {
        return (loc.getWorld() != null ? loc.getWorld().getName() : "world") + "," +
                loc.getBlockX() + "," +
                loc.getBlockY() + "," +
                loc.getBlockZ();
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @Nullable Location deserialize(@NonNull String str) {
        String[] parts = str.split(",");
        if (parts.length < 4) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull String serializeRegion(@NonNull Location loc) {
        return serialize(loc) + "," + (loc.getBlockX() >> 4) + "," + (loc.getBlockZ() >> 4);
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull String serializeVector(@NonNull Vector vector) {
        return vector.getX() + "," + vector.getY() + "," + vector.getZ();
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @Nullable Vector deserializeVector(@NonNull String str) {
        String[] parts = str.split(",");
        if (parts.length < 3) return null;
        try {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            return new Vector(x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @CheckReturnValue
    public static double distance2D(@NonNull Location a, @NonNull Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    @CheckReturnValue
    public static @NonNull Location midpoint(@NonNull Location a, @NonNull Location b) {
        Vector3D v1 = Vector3D.fromLocation(a);
        Vector3D v2 = Vector3D.fromLocation(b);
        Vector3D mid = v1.lerp(v2, 0.5);
        return mid.toLocation(a.getWorld(), (a.getYaw() + b.getYaw()) / 2.0f, (a.getPitch() + b.getPitch()) / 2.0f);
    }

    @CheckReturnValue
    public static @NonNull Location centerBlock(@NonNull Location block) {
        return new Location(
                block.getWorld(),
                block.getBlockX() + 0.5,
                block.getBlockY() + 0.5,
                block.getBlockZ() + 0.5,
                block.getYaw(),
                block.getPitch()
        );
    }

    @CheckReturnValue
    public static boolean isWithinAABB(@NonNull Location loc, @NonNull Location min, @NonNull Location max) {
        Volume3D.AABB3D aabb = new Volume3D.AABB3D(
                new Vector3D(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ())),
                new Vector3D(Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ()))
        );
        return aabb.contains(Vector3D.fromLocation(loc));
    }

    @CheckReturnValue
    public static @NonNull Location randomInRadius(@NonNull Location center, double radius) {
        double angle = Chance.randomIn(0.0, 2.0 * Math.PI);
        double r = Chance.randomIn(0.0, radius);
        double x = center.getX() + r * FastMath.cos(angle);
        double z = center.getZ() + r * FastMath.sin(angle);
        return new Location(center.getWorld(), x, center.getY(), z, center.getYaw(), center.getPitch());
    }
}
