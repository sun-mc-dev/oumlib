package dev.oum.oumlib.text;

import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.text.placeholder.PlaceholderResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static dev.oum.oumlib.text.Preset.*;

public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Text() {
    }

    public static void send(@NonNull Audience audience, @NonNull String message, Object... pairs) {
        audience.sendMessage(parse(resolve(message, audience), createResolvers(pairs)));
    }

    public static void send(@NonNull Audience audience, @NonNull String message) {
        audience.sendMessage(parse(resolve(message, audience)));
    }

    public static void send(@NonNull Audience audience, @NonNull String message, @NonNull Record data) {
        audience.sendMessage(parse(resolve(message, audience), createResolvers(data)));
    }

    public static void sendLines(@NonNull Audience audience, @NonNull List<String> lines, Object... pairs) {
        TagResolver[] resolvers = createResolvers(pairs);
        lines.forEach(line -> audience.sendMessage(parse(resolve(line, audience), resolvers)));
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull Component parse(@NonNull String message) {
        return MM.deserialize(message);
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull Component parse(@NonNull String message, TagResolver... resolvers) {
        return MM.deserialize(message, resolvers);
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull Component deserialize(@NonNull String message) {
        return MM.deserialize(message);
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull Component deserialize(@NonNull String message, TagResolver... resolvers) {
        return MM.deserialize(message, resolvers);
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull String serialize(@NonNull Component component) {
        return MM.serialize(component);
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull String strip(@NonNull String message) {
        return MM.stripTags(message);
    }

    public static void actionBar(@NonNull Audience audience, @NonNull String message, Object... pairs) {
        audience.sendActionBar(parse(resolve(message, audience), createResolvers(pairs)));
    }

    public static void title(@NonNull Audience audience, @NonNull String title, @NonNull String subtitle,
                             @NonNull Duration fadeIn, @NonNull Duration stay, @NonNull Duration fadeOut) {
        audience.showTitle(Title.title(parse(title), parse(subtitle), Title.Times.times(fadeIn, stay, fadeOut)));
    }

    public static void title(@NonNull Audience audience, @NonNull String title, @NonNull String subtitle) {
        title(audience, title, subtitle, Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
    }

    public static void broadcast(@NonNull String message, Object... pairs) {
        OumLib.players().sendMessage(parse(resolve(message, null), createResolvers(pairs)));
    }

    public static void broadcast(@NonNull String message, @NonNull Record data) {
        OumLib.players().sendMessage(parse(resolve(message, null), createResolvers(data)));
    }

    public static void broadcastActionBar(@NonNull String message, Object... pairs) {
        OumLib.players().sendActionBar(parse(resolve(message, null), createResolvers(pairs)));
    }

    public static void broadcastTitle(@NonNull String title, @NonNull String subtitle, @NonNull Duration fadeIn, @NonNull Duration stay, @NonNull Duration fadeOut) {
        OumLib.players().showTitle(Title.title(parse(title), parse(subtitle), Title.Times.times(fadeIn, stay, fadeOut)));
    }

    public static void broadcastTitle(@NonNull String title, @NonNull String subtitle) {
        broadcastTitle(title, subtitle, Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500));
    }

    @Contract(value = "_ -> new", pure = true)
    @CheckReturnValue
    public static @NonNull TextBuilder builder(@NonNull String message) {
        return new TextBuilder(message);
    }

    @Contract(pure = true)
    @CheckReturnValue
    public static @NonNull Component clickable(@NonNull String text, @NonNull ClickEvent clickEvent, @Nullable String hoverText) {
        Component c = parse(text).clickEvent(clickEvent);
        if (hoverText != null) c = c.hoverEvent(HoverEvent.showText(parse(hoverText)));
        return c;
    }

    private static @NonNull TagResolver @NonNull [] createResolvers(Object @NonNull ... pairs) {
        if (pairs.length == 0) return new TagResolver[0];
        List<TagResolver> resolvers = new ArrayList<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            String key = String.valueOf(pairs[i]);
            Object val = pairs[i + 1];
            if (val instanceof Component comp) {
                resolvers.add(Placeholder.component(key, comp));
            } else {
                resolvers.add(Placeholder.parsed(key, String.valueOf(val)));
            }
        }
        return resolvers.toArray(new TagResolver[0]);
    }

    @Contract("null -> new")
    private static @NonNull TagResolver @NonNull [] createResolvers(Record data) {
        if (data == null) return new TagResolver[0];
        List<TagResolver> resolvers = new ArrayList<>();
        try {
            for (RecordComponent comp : data.getClass().getRecordComponents()) {
                Object val = comp.getAccessor().invoke(data);
                String key = comp.getName();
                if (val instanceof Component compVal) {
                    resolvers.add(Placeholder.component(key, compVal));
                } else {
                    resolvers.add(Placeholder.parsed(key, val != null ? String.valueOf(val) : ""));
                }
            }
        } catch (Exception ignored) {
        }
        return resolvers.toArray(new TagResolver[0]);
    }

    private static @NonNull String resolve(String input, Object player) {
        return PlaceholderResolver.resolveInternal(input, player);
    }

    public static @NonNull BossBar bossBar(@NonNull Audience audience, @NonNull String titleMiniMessage,
                                           float progress, BossBar.@NonNull Color color, BossBar.@NonNull Overlay overlay) {
        Component title = parse(titleMiniMessage);
        BossBar bar = BossBar.bossBar(title, progress, color, overlay);
        audience.showBossBar(bar);
        return bar;
    }

    public static void bossBarTemporary(@NonNull Audience audience, @NonNull String titleMiniMessage,
                                        float progress, BossBar.@NonNull Color color, BossBar.@NonNull Overlay overlay,
                                        @NonNull Duration duration) {
        BossBar bar = bossBar(audience, titleMiniMessage, progress, color, overlay);
        if (OumLib.isPaper() && isBukkitPlayer(audience)) {
            Scheduler.runLaterFor(audience, duration, () -> audience.hideBossBar(bar), () -> {
            });
        } else {
            Scheduler.runLater(duration, () -> audience.hideBossBar(bar));
        }
    }

    private static boolean isBukkitPlayer(Object audience) {
        try {
            return Class.forName("org.bukkit.entity.Player").isInstance(audience);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void ascii(boolean colorized, String @NonNull ... lines) {
        for (String line : lines) {
            if (colorized) {
                OumLib.console().sendMessage(MM.deserialize(line));
            } else {
                OumLib.console().sendMessage(Component.text(line));
            }
        }
    }

    public static void ascii(String... lines) {
        ascii(false, lines);
    }

    public static final class Preset {

        private Preset() {
        }

        public static void success(Audience audience, String message, Object... pairs) {
            send(audience, OumLib.presets().prefix(SUCCESS) + message, pairs);
        }

        public static void error(Audience audience, String message, Object... pairs) {
            send(audience, OumLib.presets().prefix(ERROR) + message, pairs);
        }

        public static void info(Audience audience, String message, Object... pairs) {
            send(audience, OumLib.presets().prefix(INFO) + message, pairs);
        }

        public static void warning(Audience audience, String message, Object... pairs) {
            send(audience, OumLib.presets().prefix(WARNING) + message, pairs);
        }

        public static void successBroadcast(String message, Object... pairs) {
            broadcast(OumLib.presets().prefix(SUCCESS) + message, pairs);
        }

        public static void errorBroadcast(String message, Object... pairs) {
            broadcast(OumLib.presets().prefix(ERROR) + message, pairs);
        }

        public static void infoBroadcast(String message, Object... pairs) {
            broadcast(OumLib.presets().prefix(INFO) + message, pairs);
        }

        public static void warningBroadcast(String message, Object... pairs) {
            broadcast(OumLib.presets().prefix(WARNING) + message, pairs);
        }
    }
}