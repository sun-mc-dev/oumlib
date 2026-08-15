package dev.oum.oumlib.entity.display;

import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class DisplayAnimation {

    private DisplayAnimation() {
    }

    @CheckReturnValue
    public static @NonNull TaskHandle bobbing(@NonNull VirtualDisplay display, double amplitude, int periodTicks) {
        Objects.requireNonNull(display);
        AtomicLong tickCounter = new AtomicLong(0);
        Vector baseTranslation = display.getTranslation();

        return Scheduler.runRepeating(Duration.ZERO, Duration.ofMillis(50), () -> {
            long t = tickCounter.incrementAndGet();
            double offset = Math.sin((2 * Math.PI * (t % periodTicks)) / periodTicks) * amplitude;
            display.setTranslation(new Vector(baseTranslation.getX(), baseTranslation.getY() + offset, baseTranslation.getZ()));
            display.updateMetadata();
        });
    }

    @CheckReturnValue
    public static @NonNull TaskHandle spin(@NonNull VirtualDisplay display, float yawIncrementPerTick) {
        Objects.requireNonNull(display);
        return Scheduler.runRepeating(Duration.ZERO, Duration.ofMillis(50), () -> {
            Location loc = display.getLocation();
            float newYaw = (loc.getYaw() + yawIncrementPerTick) % 360f;
            loc.setYaw(newYaw);
            display.teleport(loc);
        });
    }

    @CheckReturnValue
    public static @NonNull TaskHandle pulse(@NonNull VirtualDisplay display, double minScale, double maxScale, int periodTicks) {
        Objects.requireNonNull(display);
        AtomicLong tickCounter = new AtomicLong(0);

        return Scheduler.runRepeating(Duration.ZERO, Duration.ofMillis(50), () -> {
            long t = tickCounter.incrementAndGet();
            double progress = (Math.sin((2 * Math.PI * (t % periodTicks)) / periodTicks) + 1.0) / 2.0;
            double s = minScale + (maxScale - minScale) * progress;
            display.setScale(new Vector(s, s, s));
            display.updateMetadata();
        });
    }
}
