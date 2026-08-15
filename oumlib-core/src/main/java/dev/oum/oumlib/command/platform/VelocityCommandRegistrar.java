package dev.oum.oumlib.command.platform;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.bridge.permission.Permission;
import dev.oum.oumlib.command.*;
import dev.oum.oumlib.cooldown.CooldownManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class VelocityCommandRegistrar implements CommandRegistrar {

    @Override
    public void register(@NonNull CommandBuilder builder) {
        CommandManager commandManager = OumLib.proxy().getCommandManager();
        CommandMeta meta = commandManager.metaBuilder(builder.label())
                .aliases(builder.aliases().toArray(new String[0]))
                .build();

        BrigadierCommand command = new BrigadierCommand(buildNode(builder).build());
        commandManager.register(meta, command);
    }

    private @NonNull LiteralArgumentBuilder<CommandSource> buildNode(@NonNull CommandBuilder builder) {
        var root = LiteralArgumentBuilder.<CommandSource>literal(builder.label());

        if (builder.permissionObject() != null) {
            Permission permObj = builder.permissionObject();
            root.requires(permObj::has);
        } else if (builder.permission() != null) {
            String perm = builder.permission();
            root.requires(source -> source.hasPermission(perm));
        }

        for (SubcommandBuilder sub : builder.subcommands()) {
            root.then(buildSubNode(sub, sub.label(), builder));
            for (String alias : sub.aliases()) {
                root.then(buildSubNode(sub, alias, builder));
            }
        }

        if (builder.executor() != null) {
            attachArguments(root, builder.arguments(), builder.executor(), builder.label(),
                    builder.permission(), builder.cooldownManager(), builder.cooldownDuration(),
                    builder.cooldownMessage(), builder.cooldownBypass(), builder.exceptionHandler());
        }

        return root;
    }

    private @NonNull LiteralArgumentBuilder<CommandSource> buildSubNode(
            @NonNull SubcommandBuilder sub,
            @NonNull String label,
            @NonNull CommandBuilder builder
    ) {
        var subLiteral = LiteralArgumentBuilder.<CommandSource>literal(label);
        if (sub.permissionObject() != null) {
            Permission subPermObj = sub.permissionObject();
            subLiteral.requires(subPermObj::has);
        } else if (sub.permission() != null) {
            String subPerm = sub.permission();
            subLiteral.requires(source -> source.hasPermission(subPerm));
        }

        for (SubcommandBuilder child : sub.subcommands()) {
            subLiteral.then(buildSubNode(child, child.label(), builder));
            for (String alias : child.aliases()) {
                subLiteral.then(buildSubNode(child, alias, builder));
            }
        }

        if (sub.executor() != null) {
            CooldownManager<UUID> cdMgr = sub.cooldownManager() != null ? sub.cooldownManager() : builder.cooldownManager();
            Duration cdDur = sub.cooldownDuration() != null ? sub.cooldownDuration() : builder.cooldownDuration();
            String cdMsg = sub.cooldownDuration() != null ? sub.cooldownMessage() : builder.cooldownMessage();
            Predicate<CommandContext> cdBypass = sub.cooldownBypass() != null ? sub.cooldownBypass() : builder.cooldownBypass();
            BiConsumer<CommandContext, Throwable> exHandler = sub.exceptionHandler() != null ? sub.exceptionHandler() : builder.exceptionHandler();
            String perm = sub.permission() != null ? sub.permission() : builder.permission();

            attachArguments(subLiteral, sub.arguments(), sub.executor(), builder.label() + " " + sub.label(),
                    perm, cdMgr, cdDur, cdMsg, cdBypass, exHandler);
        }
        return subLiteral;
    }

    private void attachArguments(
            LiteralArgumentBuilder<CommandSource> node,
            @NonNull List<Argument<?>> args,
            Consumer<CommandContext> exec,
            String fullLabel,
            String permission,
            CooldownManager<UUID> cdMgr,
            Duration cdDur,
            String cdMsg,
            Predicate<CommandContext> cdBypass,
            BiConsumer<CommandContext, Throwable> exHandler
    ) {
        if (args.isEmpty()) {
            node.executes(ctx -> {
                handleExecution(ctx.getSource(), new ArgumentMap(ctx), exec, fullLabel,
                        permission, cdMgr, cdDur, cdMsg, cdBypass, exHandler);
                return 1;
            });
            return;
        }

        RequiredArgumentBuilder<CommandSource, ?> first = buildArgChain(args, exec, fullLabel,
                permission, cdMgr, cdDur, cdMsg, cdBypass, exHandler);
        node.then(first);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private RequiredArgumentBuilder<CommandSource, ?> buildArgChain(
            @NonNull List<Argument<?>> args,
            Consumer<CommandContext> exec,
            String fullLabel,
            String permission,
            CooldownManager<UUID> cdMgr,
            Duration cdDur,
            String cdMsg,
            Predicate<CommandContext> cdBypass,
            BiConsumer<CommandContext, Throwable> exHandler
    ) {
        RequiredArgumentBuilder head = null;
        RequiredArgumentBuilder prev = null;

        for (int i = 0; i < args.size(); i++) {
            Argument<?> arg = args.get(i);
            RequiredArgumentBuilder current = RequiredArgumentBuilder.argument(
                    arg.name(), arg.brigadierType()
            );
            if (arg.suggestionProvider() != null) {
                current.suggests(arg.suggestionProvider());
            }
            if (i == args.size() - 1) {
                current.executes(ctx -> {
                    handleExecution((CommandSource) ctx.getSource(), new ArgumentMap(ctx), exec, fullLabel,
                            permission, cdMgr, cdDur, cdMsg, cdBypass, exHandler);
                    return 1;
                });
            }
            if (head == null) {
                head = current;
            } else {
                prev.then(current);
            }
            prev = current;
        }

        return head;
    }

    private void handleExecution(
            CommandSource source,
            ArgumentMap map,
            Consumer<CommandContext> exec,
            String label,
            String permission,
            CooldownManager<UUID> cdMgr,
            Duration cdDur,
            String cdMsg,
            Predicate<CommandContext> cdBypass,
            BiConsumer<CommandContext, Throwable> exHandler
    ) {
        CommandContext context = new CommandContext(source, source, label, map);
        if (source instanceof Player player) {
            if (cdMgr != null && cdDur != null) {
                boolean bypassed = (cdBypass != null)
                        ? cdBypass.test(context)
                        : player.hasPermission((permission != null ? permission : label.replace(' ', '.')) + ".bypass");
                if (!bypassed && cdMgr.isOnCooldown(player.getUniqueId())) {
                    String remaining = cdMgr.formatRemaining(player.getUniqueId());
                    player.sendMessage(MiniMessage.miniMessage()
                            .deserialize(cdMsg.replace("<remaining>", remaining)));
                    return;
                }
                cdMgr.apply(player.getUniqueId(), cdDur);
            }
        }
        try {
            exec.accept(context);
        } catch (Throwable ex) {
            BiConsumer<CommandContext, Throwable> handler = exHandler != null
                    ? exHandler
                    : OumLib.commandErrorHandler();
            if (handler != null) {
                handler.accept(context, ex);
            } else {
                OumLib.logError("Unhandled exception executing command /" + label, ex);
            }
        }
    }
}