package dev.oum.oumlib.pdc;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface PdcChangeListener {
    void onChange(@NonNull Object target, @NonNull NamespacedKey key, @Nullable Object oldValue, @Nullable Object newValue);
}
