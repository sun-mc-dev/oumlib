package dev.oum.oumlib.command;

import dev.oum.oumlib.util.Permission;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class SubcommandBuilder {

    private final List<Argument<?>> arguments = new ArrayList<>();
    private final List<String> aliases = new ArrayList<>();
    private final List<SubcommandBuilder> subcommands = new ArrayList<>();
    private String label;
    private String permission;
    private Permission permissionObject;
    private Consumer<CommandContext> executor;

    @CheckReturnValue
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

    @CheckReturnValue
    public @NonNull SubcommandBuilder label(@NonNull String label) {
        this.label = label;
        return this;
    }

    @CheckReturnValue
    @Deprecated(since = "1.0.5")
    public @NonNull SubcommandBuilder permission(@NonNull String permission) {
        this.permission = permission;
        return this;
    }

    @CheckReturnValue
    public @NonNull SubcommandBuilder permission(@NonNull Permission permission) {
        this.permissionObject = permission;
        this.permission = permission.name();
        return this;
    }

    @CheckReturnValue
    public @NonNull SubcommandBuilder argument(@NonNull Argument<?> argument) {
        arguments.add(argument);
        return this;
    }

    @Contract("_ -> this")
    @CheckReturnValue
    public @NonNull SubcommandBuilder subcommand(@NonNull Consumer<@NonNull SubcommandBuilder> configurer) {
        SubcommandBuilder sub = new SubcommandBuilder();
        configurer.accept(sub);
        subcommands.add(sub);
        return this;
    }

    @CheckReturnValue
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