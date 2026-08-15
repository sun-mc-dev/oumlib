package dev.oum.oumlib.pdc;

import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class PdcProperty<P, C> {

    private final PersistentDataHolder holder;
    private final DataKey<P, C> key;
    private final C defaultValue;
    private final List<BiConsumer<C, C>> observers = new CopyOnWriteArrayList<>();

    public PdcProperty(@NonNull PersistentDataHolder holder, @NonNull DataKey<P, C> key, @Nullable C defaultValue) {
        this.holder = Objects.requireNonNull(holder);
        this.key = Objects.requireNonNull(key);
        this.defaultValue = defaultValue;
    }

    @CheckReturnValue
    public @Nullable C get() {
        C value = holder.getPersistentDataContainer().get(key.key(), key.type());
        return value != null ? value : defaultValue;
    }

    @CheckReturnValue
    public @NonNull Optional<C> getOptional() {
        return Optional.ofNullable(get());
    }

    public void set(@Nullable C newValue) {
        C oldValue = get();
        if (newValue == null) {
            holder.getPersistentDataContainer().remove(key.key());
        } else {
            holder.getPersistentDataContainer().set(key.key(), key.type(), newValue);
        }
        PDC.triggerListeners(holder, key.key(), oldValue, newValue);
        for (BiConsumer<C, C> observer : observers) {
            try {
                observer.accept(oldValue, newValue);
            } catch (Exception ignored) {
            }
        }
    }

    public void update(@NonNull UnaryOperator<C> updater) {
        set(updater.apply(get()));
    }

    public @NonNull PdcProperty<P, C> observe(@NonNull BiConsumer<C, C> observer) {
        observers.add(Objects.requireNonNull(observer));
        return this;
    }

    public @NonNull PdcProperty<P, C> bindTo(@NonNull Consumer<C> consumer) {
        Objects.requireNonNull(consumer);
        consumer.accept(get());
        observers.add((oldVal, newVal) -> consumer.accept(newVal));
        return this;
    }

    @CheckReturnValue
    public @NonNull PersistentDataHolder holder() {
        return holder;
    }

    @CheckReturnValue
    public @NonNull DataKey<P, C> key() {
        return key;
    }
}
