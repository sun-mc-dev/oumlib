package dev.oum.oumlib.command;

import com.mojang.brigadier.context.CommandContext;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class ArgumentMap {

    private final CommandContext<?> ctx;
    private final Map<String, Object> cache = new HashMap<>();

    public ArgumentMap(CommandContext<?> ctx) {
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull Argument<T> argument) {
        return (T) cache.computeIfAbsent(argument.name(), k -> {
            try {
                Object raw = ctx.getArgument(k, Object.class);
                return argument.extractor().apply(raw, ctx);
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    public <T> T getOrDefault(@NonNull Argument<T> argument, T defaultValue) {
        T val = get(argument);
        return val != null ? val : defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull String name) {
        return (T) cache.computeIfAbsent(name, k -> {
            try {
                return ctx.getArgument(k, Object.class);
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull String name, @NonNull Class<T> clazz) {
        return (T) cache.computeIfAbsent(name, k -> {
            try {
                return ctx.getArgument(k, clazz);
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    public @NonNull String getString(@NonNull String name) {
        String val = get(name, String.class);
        return val != null ? val : "";
    }

    public int getInt(@NonNull String name) {
        Integer val = get(name, Integer.class);
        return val != null ? val : 0;
    }

    public double getDouble(@NonNull String name) {
        Double val = get(name, Double.class);
        return val != null ? val : 0.0;
    }

    public boolean getBoolean(@NonNull String name) {
        Boolean val = get(name, Boolean.class);
        return val != null && val;
    }

    public CommandContext<?> raw() {
        return ctx;
    }
}