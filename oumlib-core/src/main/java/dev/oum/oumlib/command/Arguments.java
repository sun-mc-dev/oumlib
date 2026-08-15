package dev.oum.oumlib.command;

import com.mojang.brigadier.arguments.*;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.text.Format;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Arguments {

    private Arguments() {
    }

    @Contract("_ -> new")
    public static @NonNull Argument<String> word(String name) {
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> (String) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<String> string(String name) {
        return new Argument<>(name, StringArgumentType.greedyString(), (raw, ctx) -> (String) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Integer> integer(String name) {
        return new Argument<>(name, IntegerArgumentType.integer(), (raw, ctx) -> (Integer) raw);
    }

    @Contract("_, _, _ -> new")
    public static @NonNull Argument<Integer> integer(String name, int min, int max) {
        return new Argument<>(name, IntegerArgumentType.integer(min, max), (raw, ctx) -> (Integer) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Double> decimal(String name) {
        return new Argument<>(name, DoubleArgumentType.doubleArg(), (raw, ctx) -> (Double) raw);
    }

    @Contract("_, _, _ -> new")
    public static @NonNull Argument<Double> decimal(String name, double min, double max) {
        return new Argument<>(name, DoubleArgumentType.doubleArg(min, max), (raw, ctx) -> (Double) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Boolean> bool(String name) {
        return new Argument<>(name, BoolArgumentType.bool(), (raw, ctx) -> (Boolean) raw);
    }

    @Contract("_ -> new")
    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public static <T> @NonNull Argument<T> player(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<T>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createPlayerArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }
        try {
            Class.forName("org.bukkit.Bukkit");
            return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
                String nameStr = (String) raw;
                return (T) Bukkit.getPlayer(nameStr);
            });
        } catch (Exception ignored) {
        }
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
            String nameStr = (String) raw;
            return (T) OumLib.proxy().getPlayer(nameStr).orElse(null);
        });
    }

    @Contract("_ -> new")
    @SuppressWarnings("unchecked")
    public static <T> @NonNull Argument<T> finePosition(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<T>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createFinePositionArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }
        throw new UnsupportedOperationException("Fine position argument is only supported on Paper.");
    }

    @Contract("_ -> new")
    @SuppressWarnings("unchecked")
    public static <T> @NonNull Argument<T> blockPosition(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<T>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createBlockPositionArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }
        throw new UnsupportedOperationException("Block position argument is only supported on Paper.");
    }

    @Contract("_ -> new")
    @SuppressWarnings("unchecked")
    public static <T> @NonNull Argument<T> players(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<T>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createPlayersArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }
        try {
            Class.forName("org.bukkit.Bukkit");
            return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
                String nameStr = (String) raw;
                var player = Bukkit.getPlayer(nameStr);
                return (T) (player != null ? List.of(player) : List.of());
            });
        } catch (Exception ignored) {
        }
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
            String nameStr = (String) raw;
            var player = OumLib.proxy().getPlayer(nameStr).orElse(null);
            return (T) (player != null ? List.of(player) : List.of());
        });
    }

    @Contract("_ -> new")
    @SuppressWarnings("unchecked")
    public static <T> @NonNull Argument<T> world(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<T>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createWorldArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }
        throw new UnsupportedOperationException("World argument is only supported on Paper.");
    }

    @Contract("_ -> new")
    @SuppressWarnings("unchecked")
    public static <T> @NonNull Argument<T> key(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<T>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createKeyArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }
        throw new UnsupportedOperationException("Key/NamespacedKey argument is only supported on Paper.");
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Material> material(String name) {
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
            try {
                return Material.valueOf(raw.toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    @Contract("_, _ -> new")
    public static <E extends Enum<E>> @NonNull Argument<E> enumValue(String name, Class<E> type) {
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
            try {
                return Enum.valueOf(type, raw.toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Duration> duration(String name) {
        return new Argument<>(name, StringArgumentType.word(), (raw, ctx)
                -> Format.parseDuration(raw.toString()));
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Float> floatArg(String name) {
        return new Argument<>(name, FloatArgumentType.floatArg(), (raw, ctx) -> (Float) raw);
    }

    @Contract("_, _, _ -> new")
    public static @NonNull Argument<Float> floatArg(String name, float min, float max) {
        return new Argument<>(name, FloatArgumentType.floatArg(min, max), (raw, ctx) -> (Float) raw);
    }

    @Contract("_ -> new")
    public static @NonNull Argument<Long> longArg(String name) {
        return new Argument<>(name, LongArgumentType.longArg(), (raw, ctx) -> (Long) raw);
    }

    @Contract("_, _, _ -> new")
    public static @NonNull Argument<Long> longArg(String name, long min, long max) {
        return new Argument<>(name, LongArgumentType.longArg(min, max), (raw, ctx) -> (Long) raw);
    }

    @Contract("_ -> new")
    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public static <T> @NonNull Argument<T> offlinePlayer(String name) {
        try {
            Class.forName("org.bukkit.Bukkit");
            return (Argument<T>) new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> {
                String nameStr = (String) raw;
                return Bukkit.getOfflinePlayer(nameStr);
            }).suggests(context -> {
                try {
                    List<String> suggestions = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        suggestions.add(p.getName());
                    }
                    return suggestions;
                } catch (Throwable t) {
                    return List.of();
                }
            });
        } catch (Exception ignored) {
        }
        return (Argument<T>) new Argument<>(name, StringArgumentType.word(), (raw, ctx) -> (String) raw);
    }

    @Contract("_ -> new")
    @SuppressWarnings("unchecked")
    public static <T> @NonNull Argument<T> entity(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<T>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createEntityArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }
        throw new UnsupportedOperationException("Entity argument is only supported on Paper.");
    }

    @Contract("_ -> new")
    @SuppressWarnings("unchecked")
    public static <T> @NonNull Argument<T> entities(String name) {
        try {
            Class.forName("io.papermc.paper.command.brigadier.argument.ArgumentTypes");
            return (Argument<T>) Class.forName("dev.oum.oumlib.command.platform.PaperCommandHelper")
                    .getDeclaredMethod("createEntitiesArgument", String.class)
                    .invoke(null, name);
        } catch (Exception ignored) {
        }
        throw new UnsupportedOperationException("Entities argument is only supported on Paper.");
    }

    @Deprecated(since = "1.0.1", forRemoval = true)
    public static @NonNull Duration parseDuration(@NonNull String input) {
        return Format.parseDuration(input);
    }
}