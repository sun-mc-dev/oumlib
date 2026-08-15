package dev.oum.oumlib.command;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.oum.oumlib.scheduler.Scheduler;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class Argument<T> {

    private static final Map<UUID, CompletableFuture<List<String>>> pendingSuggestions = new ConcurrentHashMap<>();

    private final String name;
    private final ArgumentType<?> brigadierType;
    private final BiFunction<Object, com.mojang.brigadier.context.CommandContext<?>, T> extractor;
    private SuggestionProvider<?> suggestionProvider;

    public Argument(@NonNull String name, @NonNull ArgumentType<?> brigadierType,
                    @NonNull BiFunction<Object, com.mojang.brigadier.context.CommandContext<?>, T> extractor) {
        this.name = name;
        this.brigadierType = brigadierType;
        this.extractor = extractor;
    }

    public static void handleVelocityPluginMessage(@NonNull UUID playerUuid, @NonNull List<String> suggestions) {
        CompletableFuture<List<String>> future = pendingSuggestions.remove(playerUuid);
        if (future != null) {
            future.complete(suggestions);
        }
    }

    @Contract(value = "_ -> this", mutates = "this")
    public <S> @NonNull Argument<T> suggests(@Nullable SuggestionProvider<S> provider) {
        this.suggestionProvider = provider;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull Argument<T> suggests(@NonNull Function<CommandContext, Collection<String>> provider) {
        this.suggestionProvider = (ctx, builder) -> {
            var oumCtx = CommandContext.fromBrigadier(ctx);
            for (String s : provider.apply(oumCtx)) {
                builder.suggest(s);
            }
            return builder.buildFuture();
        };
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull Argument<T> suggestsRich(@NonNull Function<CommandContext, Collection<RichSuggestion>> provider) {
        this.suggestionProvider = (ctx, builder) -> {
            var oumCtx = CommandContext.fromBrigadier(ctx);
            for (RichSuggestion s : provider.apply(oumCtx)) {
                if (s.tooltip() != null) {
                    Message tooltipMsg = s::tooltip;
                    builder.suggest(s.value(), tooltipMsg);
                } else {
                    builder.suggest(s.value());
                }
            }
            return builder.buildFuture();
        };
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull Argument<T> suggestsAsync(@NonNull Function<CommandContext, CompletableFuture<Collection<String>>> provider) {
        this.suggestionProvider = (ctx, builder) -> {
            var oumCtx = CommandContext.fromBrigadier(ctx);
            return provider.apply(oumCtx).thenApply(suggestions -> {
                for (String s : suggestions) {
                    builder.suggest(s);
                }
                return builder.build();
            });
        };
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull Argument<T> suggestsCached(
            @NonNull Function<CommandContext, Collection<String>> provider,
            @NonNull Duration cacheDuration
    ) {
        final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
        this.suggestionProvider = (ctx, builder) -> {
            var oumCtx = CommandContext.fromBrigadier(ctx);
            String cacheKey = oumCtx.sender().toString();
            long now = System.currentTimeMillis();
            CacheEntry entry = cache.get(cacheKey);
            if (entry != null && now - entry.timestamp < cacheDuration.toMillis()) {
                for (String s : entry.suggestions) {
                    builder.suggest(s);
                }
                return builder.buildFuture();
            }
            Collection<String> suggestions = provider.apply(oumCtx);
            cache.put(cacheKey, new CacheEntry(suggestions, now));
            for (String s : suggestions) {
                builder.suggest(s);
            }
            return builder.buildFuture();
        };
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull Argument<T> suggestsCachedAsync(
            @NonNull Function<CommandContext, CompletableFuture<Collection<String>>> provider,
            @NonNull Duration cacheDuration
    ) {
        final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
        this.suggestionProvider = (ctx, builder) -> {
            var oumCtx = CommandContext.fromBrigadier(ctx);
            String cacheKey = oumCtx.sender().toString();
            long now = System.currentTimeMillis();
            CacheEntry entry = cache.get(cacheKey);
            if (entry != null && now - entry.timestamp < cacheDuration.toMillis()) {
                for (String s : entry.suggestions) {
                    builder.suggest(s);
                }
                return builder.buildFuture();
            }
            return provider.apply(oumCtx).thenApply(suggestions -> {
                cache.put(cacheKey, new CacheEntry(suggestions, now));
                for (String s : suggestions) {
                    builder.suggest(s);
                }
                return builder.build();
            });
        };
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull Argument<T> suggestsVelocitySpigot(@NonNull String queryType) {
        this.suggestionProvider = (ctx, builder) -> {
            var oumCtx = CommandContext.fromBrigadier(ctx);
            if (!oumCtx.isPlayer()) {
                return builder.buildFuture();
            }
            Object playerObj = oumCtx.source();
            try {
                UUID uuid = (UUID) playerObj.getClass().getMethod("getUniqueId").invoke(playerObj);
                CompletableFuture<List<String>> future = new CompletableFuture<>();
                pendingSuggestions.put(uuid, future);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeUTF(uuid.toString());
                dos.writeUTF(queryType);
                dos.writeUTF(builder.getRemaining());

                Class<?> proxyClass = Class.forName("dev.oum.oumlib.proxy.Proxy");
                Class<?> playerInterface = Class.forName("com.velocitypowered.api.proxy.Player");
                proxyClass.getMethod("sendPluginMessage", playerInterface, String.class, byte[].class)
                        .invoke(null, playerObj, "oumlib:autocomplete", baos.toByteArray());

                Scheduler.runLater(Duration.ofMillis(500), () -> {
                    var f = pendingSuggestions.remove(uuid);
                    if (f != null) {
                        f.complete(List.of());
                    }
                });

                return future.thenApply(suggestions -> {
                    for (String s : suggestions) {
                        builder.suggest(s);
                    }
                    return builder.build();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return builder.buildFuture();
        };
        return this;
    }

    @CheckReturnValue
    public @NonNull String name() {
        return name;
    }

    @CheckReturnValue
    public @NonNull ArgumentType<?> brigadierType() {
        return brigadierType;
    }

    @CheckReturnValue
    public @NonNull BiFunction<Object, com.mojang.brigadier.context.CommandContext<?>, T> extractor() {
        return extractor;
    }

    @CheckReturnValue
    @SuppressWarnings("unchecked")
    public <S> @Nullable SuggestionProvider<S> suggestionProvider() {
        return (SuggestionProvider<S>) suggestionProvider;
    }

    private static record CacheEntry(Collection<String> suggestions, long timestamp) {
    }
}