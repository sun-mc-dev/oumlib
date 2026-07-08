package dev.oum.oumlib.scheduler;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public final class StaggeredTask<T> {

    private final Queue<T> queue;
    private final Consumer<T> action;
    private final int batchSize;
    private final Duration interval;
    private TaskHandle handle;

    private StaggeredTask(Collection<T> items, Consumer<T> action, int batchSize, Duration interval) {
        this.queue = new ConcurrentLinkedQueue<>(items);
        this.action = action;
        this.batchSize = batchSize;
        this.interval = interval;
    }

    public static <T> @NonNull StaggeredTask<T> of(@NonNull Collection<T> items, @NonNull Consumer<T> action, int batchSize, @NonNull Duration interval) {
        return new StaggeredTask<>(items, action, batchSize, interval);
    }

    public @NonNull StaggeredTask<T> start() {
        if (handle != null) {
            return this;
        }
        handle = Scheduler.runRepeating(Duration.ZERO, interval, () -> {
            for (int i = 0; i < batchSize; i++) {
                T item = queue.poll();
                if (item == null) {
                    cancel();
                    break;
                }
                try {
                    action.accept(item);
                } catch (Exception ignored) {
                }
            }
        });
        return this;
    }

    public void cancel() {
        if (handle != null) {
            handle.cancel();
            handle = null;
        }
    }

    public boolean isRunning() {
        return handle != null;
    }
}
