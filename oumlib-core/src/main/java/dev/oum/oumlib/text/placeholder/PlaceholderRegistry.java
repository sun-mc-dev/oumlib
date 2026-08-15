package dev.oum.oumlib.text.placeholder;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.text.placeholder.bridge.MiniPlaceholdersHelper;
import dev.oum.oumlib.text.placeholder.bridge.PapiHelper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PlaceholderRegistry {

    private final Map<String, Map<String, PlaceholderSupplier>> namespaces = new HashMap<>();
    private String currentNamespace;

    public PlaceholderRegistry forNamespace(String namespace) {
        this.currentNamespace = namespace;
        namespaces.putIfAbsent(namespace, new HashMap<>());
        return this;
    }

    public PlaceholderRegistry add(String key, Function<Object, String> fn) {
        ensureNamespace();
        namespaces.get(currentNamespace).put(key, PlaceholderSupplier.ofPlayer(fn));
        return this;
    }

    public <P> PlaceholderRegistry add(String key, Class<P> playerClass, Function<P, String> fn) {
        ensureNamespace();
        namespaces.get(currentNamespace).put(key, PlaceholderSupplier.ofPlayer(obj -> {
            if (playerClass.isInstance(obj)) {
                return fn.apply(playerClass.cast(obj));
            }
            return "";
        }));
        return this;
    }

    public PlaceholderRegistry add(String key, BiFunction<Object, Map<String, String>, String> fn) {
        ensureNamespace();
        namespaces.get(currentNamespace).put(key, PlaceholderSupplier.ofParam(fn));
        return this;
    }

    public PlaceholderRegistry addGlobal(String key, Supplier<String> fn) {
        ensureNamespace();
        namespaces.get(currentNamespace).put(key, PlaceholderSupplier.ofGlobal(fn));
        return this;
    }

    public void register() {
        try {
            Class.forName("org.bukkit.Bukkit");
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            PapiHelper.register((Plugin) OumLib.plugin(), this);
        } catch (Throwable ignored) {
        }

        try {
            Class.forName("io.github.miniplaceholders.api.Expansion");
            MiniPlaceholdersHelper.register(this);
        } catch (Throwable ignored) {
        }
    }

    public @Nullable String resolve(String namespace, String key, Object player, Map<String, String> params) {
        Map<String, PlaceholderSupplier> suppliers = namespaces.get(namespace);
        if (suppliers == null) return null;
        PlaceholderSupplier supplier = suppliers.get(key);
        if (supplier == null) return null;
        return switch (supplier) {
            case PlaceholderSupplier.PlayerSupplier ps -> player != null ? ps.fn().apply(player) : null;
            case PlaceholderSupplier.ParamSupplier ps -> player != null ? ps.fn().apply(player, params) : null;
            case PlaceholderSupplier.GlobalSupplier gs -> gs.fn().get();
        };
    }

    @Contract(pure = true)
    public @NonNull @Unmodifiable Map<String, Map<String, PlaceholderSupplier>> getNamespaces() {
        return Map.copyOf(namespaces);
    }

    private void ensureNamespace() {
        if (currentNamespace == null) throw new IllegalStateException("Call forNamespace() first.");
    }
}