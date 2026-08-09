package dev.oum.oumlib.math;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Vector2D(double x, double z) {

    public static final Vector2D ZERO = new Vector2D(0.0, 0.0);
    public static final Vector2D ONE = new Vector2D(1.0, 1.0);

    @Contract(value = "_, _ -> new", pure = true)
    public static @NonNull Vector2D of(double x, double z) {
        return new Vector2D(x, z);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector2D fromBukkit(@Nullable Vector vector) {
        if (vector == null) return ZERO;
        return new Vector2D(vector.getX(), vector.getZ());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector2D fromLocation(@Nullable Location loc) {
        if (loc == null) return ZERO;
        return new Vector2D(loc.getX(), loc.getZ());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector2D fromBlock(@Nullable Block block) {
        if (block == null) return ZERO;
        return new Vector2D(block.getX(), block.getZ());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Vector2D read(@NonNull DataInputStream dis) throws IOException {
        return new Vector2D(dis.readDouble(), dis.readDouble());
    }

    public void write(@NonNull DataOutputStream dos) throws IOException {
        dos.writeDouble(x);
        dos.writeDouble(z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector2D add(@NonNull Vector2D other) {
        return new Vector2D(x + other.x, z + other.z);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NonNull Vector2D add(double x, double z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector2D subtract(@NonNull Vector2D other) {
        return new Vector2D(x - other.x, z - other.z);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NonNull Vector2D subtract(double x, double z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, z * scalar);
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector2D divide(double scalar) {
        return new Vector2D(x / scalar, z / scalar);
    }

    public double dot(@NonNull Vector2D other) {
        return x * other.x + z * other.z;
    }

    public double cross(@NonNull Vector2D other) {
        return x * other.z - z * other.x;
    }

    public double length() {
        return Math.sqrt(x * x + z * z);
    }

    public double lengthSquared() {
        return x * x + z * z;
    }

    public double distance(@NonNull Vector2D other) {
        double dx = x - other.x;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public double distanceSquared(@NonNull Vector2D other) {
        double dx = x - other.x;
        double dz = z - other.z;
        return dx * dx + dz * dz;
    }

    @Contract(value = " -> new", pure = true)
    public @NonNull Vector2D normalize() {
        double len = length();
        if (len == 0) return ZERO;
        return new Vector2D(x / len, z / len);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NonNull Vector2D lerp(@NonNull Vector2D target, double t) {
        return new Vector2D(
                x + (target.x - x) * t,
                z + (target.z - z) * t
        );
    }

    @Contract(value = "_ -> new", pure = true)
    public @NonNull Vector toBukkitVector(double y) {
        return new Vector(x, y, z);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NonNull Location toLocation(@Nullable World world, double y) {
        return new Location(world, x, y, z);
    }

    @Contract(value = "_, _, _, _ -> new", pure = true)
    public @NonNull Location toLocation(@Nullable World world, double y, float yaw, float pitch) {
        return new Location(world, x, y, z, yaw, pitch);
    }
}
