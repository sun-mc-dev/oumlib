package dev.oum.example;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.command.CommandBuilder;
import dev.oum.oumlib.cooldown.CooldownFormatter;
import dev.oum.oumlib.cooldown.CooldownManager;
import dev.oum.oumlib.cooldown.RateLimiter;
import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.text.Text;
import dev.oum.oumlib.proxy.Proxy;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Plugin(
        id = "example-oum-plugin",
        name = "Example Oum Plugin",
        version = "1.0.8",
        description = "An example plugin using oumlib on Velocity",
        authors = {"sun-dev"}
)
public final class VelocityExamplePlugin {

    private final ProxyServer server;
    private final RateLimiter<UUID> proxyCommandLimiter = RateLimiter.slidingWindow(5, Duration.ofSeconds(2));
    private final CooldownManager<UUID> serverSwitchCooldowns = CooldownManager.<UUID>create()
            .defaultFormatter(CooldownFormatter.PRECISE);

    @Inject
    public VelocityExamplePlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        OumLib.init(server, this);
        ExampleAnnouncer.initialize();

        Proxy.registerFallbackRouter(this, List.of("lobby-1", "lobby-2", "hub"));

        Events.listen(PostLoginEvent.class, e -> {
            Player player = e.getPlayer();
            ExampleAnnouncer.handlePlayerJoin(player.getUsername());
            Text.send(player, "<gradient:#ff5555:#ffff55>Welcome to the network, " + player.getUsername() + "!</gradient>");
        });

        CommandBuilder.of("hub")
                .description("Send player back to the hub server")
                .aliases("lobby", "spawn")
                .playerOnly()
                .executes(ctx -> {
                    Player player = (Player) ctx.sender();
                    if (!proxyCommandLimiter.tryAcquire(player.getUniqueId())) {
                        Text.send(player, "<red>Please slow down!</red>");
                        return;
                    }
                    if (serverSwitchCooldowns.isOnCooldown(player.getUniqueId())) {
                        Text.send(player, "<red>Wait " + serverSwitchCooldowns.formatRemaining(player.getUniqueId()) + " before switching servers!</red>");
                        return;
                    }
                    serverSwitchCooldowns.apply(player.getUniqueId(), Duration.ofSeconds(3));
                    Text.send(player, "<green>Connecting to hub...</green>");
                    Proxy.connect(player, "hub");
                })
                .register();

        Scheduler.runRepeating(Duration.ZERO, Duration.ofMinutes(1), () -> {
            Proxy.ping("survival").then(result -> {
                if (result.online()) {
                    OumLib.logInfo("Survival server is healthy: " + result.currentPlayers() + "/" + result.maxPlayers() + " players.");
                } else {
                    OumLib.logWarning("Survival server appears unreachable!");
                }
            });
        });
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        OumLib.shutdown();
    }
}
