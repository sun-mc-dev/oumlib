package dev.oum.oumlib.scheduler.platform;

import dev.oum.oumlib.scheduler.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

public final class BukkitSchedulerAdapter implements SchedulerAdapter {

    private static final boolean FOLIA;
    private static BukkitSchedulerAdapter instance;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            folia = true;
        } catch (ClassNotFoundException ignored) {
        }
        FOLIA = folia;
    }

    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    private BukkitSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = Bukkit.getScheduler();
    }

    public static void initialize(Plugin plugin) {
        instance = new BukkitSchedulerAdapter(plugin);
    }

    public static BukkitSchedulerAdapter get() {
        if (instance == null) throw new IllegalStateException("BukkitSchedulerAdapter not initialized.");
        return instance;
    }

    @Contract("_ -> new")
    @Override
    public @NonNull TaskHandle run(Runnable task) {
        if (FOLIA) {
            var scheduled = Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
            return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
        }
        var scheduled = scheduler.runTask(plugin, task);
        return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runLater(Duration delay, Runnable task) {
        return runLater(toTicks(delay), task);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runLater(long ticks, Runnable task) {
        if (FOLIA) {
            var scheduled = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), ticks);
            return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
        }
        var scheduled = scheduler.runTaskLater(plugin, task, ticks);
        return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeating(Duration initialDelay, Duration interval, Runnable task) {
        return runRepeating(toTicks(initialDelay), toTicks(interval), task);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeating(long initialTicks, long periodTicks, Runnable task) {
        if (FOLIA) {
            long delay = Math.max(1L, initialTicks);
            long period = Math.max(1L, periodTicks);
            var scheduled = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), delay, period);
            return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
        }
        var scheduled = scheduler.runTaskTimer(plugin, task, initialTicks, periodTicks);
        return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
    }

    @Contract("_ -> new")
    @Override
    public @NonNull TaskHandle runAsync(Runnable task) {
        if (FOLIA) {
            var scheduled = Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
            return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
        }
        var scheduled = scheduler.runTaskAsynchronously(plugin, task);
        return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runAt(Object location, Runnable task) {
        if (location instanceof Location loc) {
            if (FOLIA) {
                var scheduled = Bukkit.getRegionScheduler().run(plugin, loc, t -> task.run());
                return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
            }
        }
        return run(task);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NonNull TaskHandle runLaterAt(Object location, Duration delay, Runnable task) {
        return runLaterAt(location, toTicks(delay), task);
    }

    @Contract("_, _, _ -> new")
    @Override
    public @NonNull TaskHandle runLaterAt(Object location, long ticks, Runnable task) {
        if (location instanceof Location loc) {
            if (FOLIA) {
                var scheduled = Bukkit.getRegionScheduler().runDelayed(plugin, loc, t -> task.run(), ticks);
                return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
            }
        }
        return runLater(ticks, task);
    }

    @Contract("_, _, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeatingAt(Object location, Duration initialDelay, Duration period, Runnable task) {
        return runRepeatingAt(location, toTicks(initialDelay), toTicks(period), task);
    }

    @Contract("_, _, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeatingAt(Object location, long initialTicks, long periodTicks, Runnable task) {
        if (location instanceof Location loc) {
            if (FOLIA) {
                long delay = Math.max(1L, initialTicks);
                long period = Math.max(1L, periodTicks);
                var scheduled = Bukkit.getRegionScheduler().runAtFixedRate(plugin, loc, t -> task.run(), delay, period);
                return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
            }
        }
        return runRepeating(initialTicks, periodTicks, task);
    }

    @Contract("_, _ -> new")
    @Override
    public @NonNull TaskHandle runFor(Object entity, Runnable task) {
        return runLaterFor(entity, 0L, task, null);
    }

    @Contract("_, _, _, _ -> new")
    @Override
    public @NonNull TaskHandle runLaterFor(Object entity, Duration delay, Runnable task, Runnable retired) {
        return runLaterFor(entity, toTicks(delay), task, retired);
    }

    @Contract("_, _, _, _ -> new")
    @Override
    public @NonNull TaskHandle runLaterFor(Object entity, long ticks, Runnable task, Runnable retired) {
        if (entity instanceof Entity ent) {
            if (FOLIA) {
                long delay = Math.max(1L, ticks);
                var scheduled = ent.getScheduler().runDelayed(plugin, t -> task.run(), retired, delay);
                if (scheduled != null) {
                    return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
                }
            }
            if (ticks <= 0) {
                return run(() -> {
                    if (ent.isValid()) {
                        task.run();
                    } else if (retired != null) {
                        retired.run();
                    }
                });
            }
            return runLater(ticks, () -> {
                if (ent.isValid()) {
                    task.run();
                } else if (retired != null) {
                    retired.run();
                }
            });
        }
        if (retired != null) {
            retired.run();
        }
        return run(task);
    }

    @Contract("_, _, _, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeatingFor(Object entity, Duration initialDelay, Duration period, Runnable task, Runnable retired) {
        return runRepeatingFor(entity, toTicks(initialDelay), toTicks(period), task, retired);
    }

    @Contract("_, _, _, _, _ -> new")
    @Override
    public @NonNull TaskHandle runRepeatingFor(Object entity, long initialTicks, long periodTicks, Runnable task, Runnable retired) {
        if (entity instanceof Entity ent) {
            if (FOLIA) {
                long delay = Math.max(1L, initialTicks);
                long period = Math.max(1L, periodTicks);
                var scheduled = ent.getScheduler().runAtFixedRate(plugin, t -> task.run(), retired, delay, period);
                if (scheduled != null) {
                    return new TaskHandle(scheduled::cancel, scheduled::isCancelled);
                }
            }
            return runRepeating(initialTicks, periodTicks, () -> {
                if (ent.isValid()) {
                    task.run();
                } else if (retired != null) {
                    retired.run();
                }
            });
        }
        if (retired != null) {
            retired.run();
        }
        return runRepeating(initialTicks, periodTicks, task);
    }

    private long toTicks(@NonNull Duration duration) {
        return Math.max(1L, duration.toMillis() / 50L);
    }
}