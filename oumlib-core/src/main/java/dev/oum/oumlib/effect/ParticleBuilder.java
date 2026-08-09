package dev.oum.oumlib.effect;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public final class ParticleBuilder {
    private final Particle particle;
    private int count = 1;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double offsetZ = 0.0;
    private double speed = 0.0;
    private Object data = null;

    public ParticleBuilder(@NonNull Particle particle) {
        this.particle = particle;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ParticleBuilder count(int count) {
        this.count = count;
        return this;
    }

    @Contract(value = "_, _, _ -> this", mutates = "this")
    public @NonNull ParticleBuilder offset(double x, double y, double z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ParticleBuilder offset(double offset) {
        return offset(offset, offset, offset);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ParticleBuilder speed(double speed) {
        this.speed = speed;
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull ParticleBuilder color(@NonNull Color color, float size) {
        if (particle == Particle.DUST) {
            this.data = new Particle.DustOptions(color, size);
        }
        return this;
    }

    @Contract(value = "_, _, _ -> this", mutates = "this")
    public @NonNull ParticleBuilder transition(@NonNull Color from, @NonNull Color to, float size) {
        if (particle == Particle.DUST_COLOR_TRANSITION) {
            this.data = new Particle.DustTransition(from, to, size);
        }
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull ParticleBuilder data(@Nullable Object data) {
        this.data = data;
        return this;
    }

    public void spawn(@NonNull Location location) {
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
        }
    }

    public void spawn(@NonNull Player player, @NonNull Location location) {
        player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data);
    }

    public void spawn(@NonNull Collection<? extends Player> players, @NonNull Location location) {
        for (Player p : players) {
            spawn(p, location);
        }
    }
}
