package dev.oum.oumlib.command;

import dev.oum.oumlib.bridge.permission.Permission;
import dev.oum.oumlib.cooldown.CooldownManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class SubcommandBuilder {

    private final List<Argument<?>> arguments = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();
    private final List<SubcommandBuilder> subcommands = new ArrayList<>();
    private String label;
    private String permission;
    private Permission permissionObject;
    private String cooldownMessage = "<red>Wait <remaining> before using this again.";
    private Consumer<CommandContext> executor;
    private Duration cooldownDuration;
    private CooldownManager<UUID> cooldownManager;
    private Predicate<CommandContext> cooldownBypass;
    private BiConsumer<CommandContext, Throwable> exceptionHandler;

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder aliases(String @NonNull ... a) {
        this.aliases.addAll(List.of(a));
        return this;
    }

    @Contract(pure = true)
    @NonNull
    @Unmodifiable
    public List<@NonNull String> aliases() {
        return List.copyOf(aliases);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder label(@NonNull String label) {
        this.label = label;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    @Deprecated(since = "1.0.5")
    public @NonNull SubcommandBuilder permission(@NonNull String permission) {
        this.permission = permission;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder permission(@NonNull Permission permission) {
        this.permissionObject = permission;
        this.permission = permission.name();
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder cooldown(@NonNull Duration duration) {
        this.cooldownDuration = duration;
        if (this.cooldownManager == null) {
            this.cooldownManager = CooldownManager.create();
        }
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public @NonNull SubcommandBuilder cooldown(@NonNull Duration duration, @NonNull CooldownManager<UUID> manager) {
        this.cooldownDuration = duration;
        this.cooldownManager = manager;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder cooldownMessage(@NonNull String message) {
        this.cooldownMessage = message;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder cooldownBypass(@NonNull Predicate<@NonNull CommandContext> predicate) {
        this.cooldownBypass = predicate;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder exceptionHandler(@NonNull BiConsumer<@NonNull CommandContext, @NonNull Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder argument(@NonNull Argument<?> argument) {
        arguments.add(argument);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder subcommand(@NonNull Consumer<@NonNull SubcommandBuilder> configurer) {
        SubcommandBuilder sub = new SubcommandBuilder();
        configurer.accept(sub);
        subcommands.add(sub);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SubcommandBuilder executes(@NonNull Consumer<@NonNull CommandContext> executor) {
        this.executor = executor;
        return this;
    }

    public @NonNull String label() {
        return label;
    }

    public @Nullable String permission() {
        return permission;
    }

    public @Nullable Permission permissionObject() {
        return permissionObject;
    }

    public @Nullable Duration cooldownDuration() {
        return cooldownDuration;
    }

    public @Nullable CooldownManager<UUID> cooldownManager() {
        return cooldownManager;
    }

    public @NonNull String cooldownMessage() {
        return cooldownMessage;
    }

    public @Nullable Predicate<CommandContext> cooldownBypass() {
        return cooldownBypass;
    }

    public @Nullable BiConsumer<CommandContext, Throwable> exceptionHandler() {
        return exceptionHandler;
    }

    @Contract(pure = true)
    @NonNull
    @Unmodifiable
    public List<@NonNull Argument<?>> arguments() {
        return List.copyOf(arguments);
    }

    @Contract(pure = true)
    @NonNull
    @Unmodifiable
    public List<@NonNull SubcommandBuilder> subcommands() {
        return List.copyOf(subcommands);
    }

    public @Nullable Consumer<@NonNull CommandContext> executor() {
        return executor;
    }
}