package dev.oum.oumlib.scheduler;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Promise<T> {

    private final CompletableFuture<T> future;

    private Promise(CompletableFuture<T> future) {
        this.future = future;
    }

    public static <U> @NonNull Promise<U> fromCompletableFuture(@NonNull CompletableFuture<U> future) {
        return new Promise<>(future);
    }

    @Contract("_ -> new")
    public static <U> @NonNull Promise<U> supplyAsync(@NonNull Supplier<U> supplier) {
        CompletableFuture<U> fut = new CompletableFuture<>();
        Scheduler.runAsync(() -> {
            try {
                fut.complete(supplier.get());
            } catch (Throwable t) {
                fut.completeExceptionally(t);
            }
        });
        return new Promise<>(fut);
    }

    public static @NonNull Promise<Void> runAsync(@NonNull Runnable runnable) {
        CompletableFuture<Void> fut = new CompletableFuture<>();
        Scheduler.runAsync(() -> {
            try {
                runnable.run();
                fut.complete(null);
            } catch (Throwable t) {
                fut.completeExceptionally(t);
            }
        });
        return new Promise<>(fut);
    }

    public static <U> @NonNull Promise<U> supplyVirtual(@NonNull Supplier<U> supplier) {
        CompletableFuture<U> fut = new CompletableFuture<>();
        Scheduler.runVirtual(() -> {
            try {
                fut.complete(supplier.get());
            } catch (Throwable t) {
                fut.completeExceptionally(t);
            }
        });
        return new Promise<>(fut);
    }

    public static @NonNull Promise<Void> runVirtual(@NonNull Runnable runnable) {
        CompletableFuture<Void> fut = new CompletableFuture<>();
        Scheduler.runVirtual(() -> {
            try {
                runnable.run();
                fut.complete(null);
            } catch (Throwable t) {
                fut.completeExceptionally(t);
            }
        });
        return new Promise<>(fut);
    }

    public static <U> @NonNull Promise<List<U>> all(@NonNull List<Promise<U>> promises) {
        CompletableFuture<?>[] futures = promises.stream()
                .map(Promise::toCompletableFuture)
                .toArray(CompletableFuture[]::new);
        CompletableFuture<List<U>> combined = CompletableFuture.allOf(futures).thenApply(ignored -> {
            List<U> results = new ArrayList<>(promises.size());
            for (Promise<U> promise : promises) {
                results.add(promise.toCompletableFuture().join());
            }
            return results;
        });
        return new Promise<>(combined);
    }

    public <U> @NonNull Promise<U> map(@NonNull Function<T, U> mapper) {
        return new Promise<>(future.thenApply(mapper));
    }

    public <U> @NonNull Promise<U> flatMap(@NonNull Function<T, Promise<U>> mapper) {
        return new Promise<>(future.thenCompose(value -> mapper.apply(value).toCompletableFuture()));
    }

    public @NonNull Promise<T> exceptionally(@NonNull Function<Throwable, T> recover) {
        return new Promise<>(future.exceptionally(recover));
    }

    public @NonNull Promise<T> exceptionallySync(@NonNull Function<Throwable, T> recover) {
        CompletableFuture<T> next = new CompletableFuture<>();
        future.whenComplete((val, err) -> {
            if (err == null) {
                next.complete(val);
            } else {
                Scheduler.run(() -> {
                    try {
                        next.complete(recover.apply(err));
                    } catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }
        });
        return new Promise<>(next);
    }

    public <U> @NonNull Promise<U> mapSync(@NonNull Function<T, U> mapper) {
        CompletableFuture<U> next = new CompletableFuture<>();
        future.whenComplete((val, err) -> {
            if (err != null) {
                next.completeExceptionally(err);
            } else {
                Scheduler.run(() -> {
                    try {
                        next.complete(mapper.apply(val));
                    } catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }
        });
        return new Promise<>(next);
    }

    public @NonNull Promise<Void> thenAccept(@NonNull Consumer<T> action) {
        return new Promise<>(future.thenAccept(action));
    }

    public @NonNull Promise<Void> thenAcceptSync(@NonNull Consumer<T> action) {
        CompletableFuture<Void> next = new CompletableFuture<>();
        future.whenComplete((val, err) -> {
            if (err != null) {
                next.completeExceptionally(err);
            } else {
                Scheduler.run(() -> {
                    try {
                        action.accept(val);
                        next.complete(null);
                    } catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }
        });
        return new Promise<>(next);
    }

    public @NonNull Promise<T> whenComplete(@Nullable Consumer<T> success, @Nullable Consumer<Throwable> failure) {
        future.whenComplete((val, err) -> {
            if (err != null) {
                if (failure != null) failure.accept(err);
            } else {
                if (success != null) success.accept(val);
            }
        });
        return this;
    }

    public @NonNull Promise<T> whenCompleteSync(@Nullable Consumer<T> success, @Nullable Consumer<Throwable> failure) {
        future.whenComplete((val, err) -> {
            Scheduler.run(() -> {
                if (err != null) {
                    if (failure != null) failure.accept(err);
                } else {
                    if (success != null) success.accept(val);
                }
            });
        });
        return this;
    }

    public <U> @NonNull Promise<U> mapSyncFor(@NonNull Object entity, @NonNull Function<T, U> mapper) {
        CompletableFuture<U> next = new CompletableFuture<>();
        future.whenComplete((val, err) -> {
            if (err != null) {
                next.completeExceptionally(err);
            } else {
                Scheduler.runFor(entity, () -> {
                    try {
                        next.complete(mapper.apply(val));
                    } catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }
        });
        return new Promise<>(next);
    }

    public @NonNull Promise<Void> thenAcceptSyncFor(@NonNull Object entity, @NonNull Consumer<T> action) {
        CompletableFuture<Void> next = new CompletableFuture<>();
        future.whenComplete((val, err) -> {
            if (err != null) {
                next.completeExceptionally(err);
            } else {
                Scheduler.runFor(entity, () -> {
                    try {
                        action.accept(val);
                        next.complete(null);
                    } catch (Throwable t) {
                        next.completeExceptionally(t);
                    }
                });
            }
        });
        return new Promise<>(next);
    }

    public @NonNull Promise<T> whenCompleteSyncFor(@NonNull Object entity, @Nullable Consumer<T> success, @Nullable Consumer<Throwable> failure) {
        future.whenComplete((val, err) -> {
            Scheduler.runFor(entity, () -> {
                if (err != null) {
                    if (failure != null) failure.accept(err);
                } else {
                    if (success != null) success.accept(val);
                }
            });
        });
        return this;
    }

    public @NonNull CompletableFuture<T> toCompletableFuture() {
        return future;
    }
}
