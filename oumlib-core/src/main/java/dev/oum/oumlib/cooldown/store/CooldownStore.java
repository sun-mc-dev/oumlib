package dev.oum.oumlib.cooldown.store;

import dev.oum.oumlib.cooldown.Cooldown;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CooldownStore<K> {

    @NonNull CompletableFuture<Void> save(@NonNull K key, @NonNull Cooldown<K> cooldown);

    @NonNull CompletableFuture<Void> remove(@NonNull K key);

    @NonNull CompletableFuture<@Nullable Cooldown<K>> load(@NonNull K key);

    @NonNull CompletableFuture<Map<K, Cooldown<K>>> loadAll();

    @NonNull CompletableFuture<Void> clear();
}
