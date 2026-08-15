package dev.oum.oumlib.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.scheduler.Promise;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Proxy {

    private static final Map<String, List<String>> SERVER_GROUPS = new ConcurrentHashMap<>();

    private Proxy() {
    }

    public static boolean connect(@NonNull Player player, @NonNull String serverName) {
        return OumLib.proxy().getServer(serverName)
                .map(server -> {
                    player.createConnectionRequest(server).fireAndForget();
                    return true;
                })
                .orElse(false);
    }

    public static void registerFallbackRouter(@NonNull Object plugin, @NonNull List<String> fallbackServerNames) {
        Events.listen(KickedFromServerEvent.class, event -> {
            String kickedFrom = event.getServer().getServerInfo().getName();

            for (String lobbyName : fallbackServerNames) {
                if (lobbyName.equalsIgnoreCase(kickedFrom)) {
                    continue;
                }

                var optionalServer = OumLib.proxy().getServer(lobbyName);
                if (optionalServer.isPresent()) {
                    event.setResult(KickedFromServerEvent.RedirectPlayer.create(
                            optionalServer.get(),
                            event.getServerKickReason().orElse(null)
                    ));
                    return;
                }
            }
        });
    }

    @CheckReturnValue
    public static int getPlayerCount(@NonNull String serverName) {
        return OumLib.proxy().getServer(serverName)
                .map(server -> server.getPlayersConnected().size())
                .orElse(0);
    }

    @CheckReturnValue
    public static int getPlayerCount(@NonNull List<String> serverNames) {
        int count = 0;
        for (String name : serverNames) {
            count += getPlayerCount(name);
        }
        return count;
    }

    public static boolean sendPluginMessage(@NonNull Player player, @NonNull String channelName, byte[] data) {
        return player.getCurrentServer()
                .map(serverConn -> {
                    MinecraftChannelIdentifier identifier = MinecraftChannelIdentifier.from(channelName);
                    OumLib.proxy().getChannelRegistrar().register(identifier);
                    return serverConn.sendPluginMessage(identifier, data);
                })
                .orElse(false);
    }

    public static void registerServer(@NonNull String name, @NonNull String ipAddress, int port) {
        OumLib.proxy().registerServer(new ServerInfo(name, new InetSocketAddress(ipAddress, port)));
    }

    public static boolean unregisterServer(@NonNull String name) {
        return OumLib.proxy().getServer(name)
                .map(server -> {
                    OumLib.proxy().unregisterServer(server.getServerInfo());
                    return true;
                })
                .orElse(false);
    }

    @CheckReturnValue
    public static @NonNull Promise<Boolean> isOnline(@NonNull String serverName) {
        return Promise.fromCompletableFuture(
                OumLib.proxy().getServer(serverName)
                        .map(server -> server.ping()
                                .thenApply(ping -> true)
                                .exceptionally(err -> false))
                        .orElseGet(() -> CompletableFuture.completedFuture(false))
        );
    }

    public static void broadcastTo(@NonNull String serverName, @NonNull String miniMessage, @NonNull TagResolver... resolvers) {
        OumLib.proxy().getServer(serverName).ifPresent(server -> {
            var component = Text.parse(miniMessage, resolvers);
            for (Player player : server.getPlayersConnected()) {
                player.sendMessage(component);
            }
        });
    }

    public static void sendTitleTo(@NonNull String serverName, @NonNull String title, @NonNull String subtitle, @NonNull TagResolver... resolvers) {
        OumLib.proxy().getServer(serverName).ifPresent(server -> {
            var titleComp = Text.parse(title, resolvers);
            var subtitleComp = Text.parse(subtitle, resolvers);
            var titleObject = Title.title(titleComp, subtitleComp);
            for (Player player : server.getPlayersConnected()) {
                player.showTitle(titleObject);
            }
        });
    }

    @CheckReturnValue
    public static @NonNull Promise<Optional<RegisteredServer>> getBestServer(@NonNull List<String> serverNames) {
        var futures = serverNames.stream()
                .map(name -> OumLib.proxy().getServer(name))
                .flatMap(Optional::stream)
                .map(server -> server.ping()
                        .thenApply(ping -> Map.entry(server, server.getPlayersConnected().size()))
                        .exceptionally(err -> null))
                .toList();

        return Promise.fromCompletableFuture(
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> {
                            RegisteredServer best = null;
                            int minPlayers = Integer.MAX_VALUE;

                            for (var future : futures) {
                                try {
                                    var entry = future.getNow(null);
                                    if (entry != null && entry.getValue() < minPlayers) {
                                        minPlayers = entry.getValue();
                                        best = entry.getKey();
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                            return Optional.ofNullable(best);
                        })
        );
    }

    public static void registerGroup(@NonNull String groupName, @NonNull List<String> serverNames) {
        SERVER_GROUPS.put(groupName.toLowerCase(Locale.ROOT), List.copyOf(serverNames));
    }

    @CheckReturnValue
    public static @NonNull List<RegisteredServer> getGroupServers(@NonNull String groupName) {
        List<String> names = SERVER_GROUPS.get(groupName.toLowerCase(Locale.ROOT));
        if (names == null) return List.of();
        return names.stream()
                .map(name -> OumLib.proxy().getServer(name))
                .flatMap(Optional::stream)
                .toList();
    }

    public static @NonNull Promise<Boolean> connectLeastPopulated(@NonNull Player player, @NonNull String groupName) {
        List<String> names = SERVER_GROUPS.get(groupName.toLowerCase(Locale.ROOT));
        if (names == null || names.isEmpty())
            return Promise.fromCompletableFuture(CompletableFuture.completedFuture(false));
        return Promise.fromCompletableFuture(
                getBestServer(names).toCompletableFuture().thenCompose(opt -> {
                    if (opt.isPresent()) {
                        player.createConnectionRequest(opt.get()).fireAndForget();
                        return CompletableFuture.completedFuture(true);
                    }
                    return CompletableFuture.completedFuture(false);
                })
        );
    }

    public static @NonNull Promise<Boolean> connectRandom(@NonNull Player player, @NonNull String groupName) {
        List<RegisteredServer> servers = getGroupServers(groupName);
        if (servers.isEmpty()) return Promise.fromCompletableFuture(CompletableFuture.completedFuture(false));
        int idx = ThreadLocalRandom.current().nextInt(servers.size());
        player.createConnectionRequest(servers.get(idx)).fireAndForget();
        return Promise.fromCompletableFuture(CompletableFuture.completedFuture(true));
    }

    public static void registerFallbackRouter(@NonNull Object plugin, @NonNull String groupName) {
        registerFallbackRouter(plugin, groupName, event -> true);
    }

    public static void registerFallbackRouter(@NonNull Object plugin, @NonNull String groupName, @NonNull Predicate<KickedFromServerEvent> filter) {
        Events.listen(KickedFromServerEvent.class, event -> {
            if (!filter.test(event)) return;
            String kickedFrom = event.getServer().getServerInfo().getName();
            List<String> lobbies = SERVER_GROUPS.get(groupName.toLowerCase(Locale.ROOT));
            if (lobbies == null || lobbies.isEmpty()) return;

            List<String> filteredLobbies = lobbies.stream()
                    .filter(name -> !name.equalsIgnoreCase(kickedFrom))
                    .toList();

            if (filteredLobbies.isEmpty()) return;

            for (String lobbyName : filteredLobbies) {
                var optionalServer = OumLib.proxy().getServer(lobbyName);
                if (optionalServer.isPresent()) {
                    event.setResult(KickedFromServerEvent.RedirectPlayer.create(
                            optionalServer.get(),
                            event.getServerKickReason().orElse(null)
                    ));
                    return;
                }
            }
        });
    }

    public static boolean sendPluginMessage(@NonNull Player player, @NonNull String channelName, @NonNull Consumer<ByteArrayDataOutput> writer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        writer.accept(out);
        return sendPluginMessage(player, channelName, out.toByteArray());
    }

    public static void broadcastPluginMessage(@NonNull String channelName, byte[] data) {
        MinecraftChannelIdentifier identifier = MinecraftChannelIdentifier.from(channelName);
        OumLib.proxy().getChannelRegistrar().register(identifier);
        for (RegisteredServer server : OumLib.proxy().getAllServers()) {
            server.sendPluginMessage(identifier, data);
        }
    }

    public static void broadcastPluginMessage(@NonNull String channelName, @NonNull Consumer<ByteArrayDataOutput> writer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        writer.accept(out);
        broadcastPluginMessage(channelName, out.toByteArray());
    }

    @CheckReturnValue
    public static @NonNull Promise<ServerPingResult> ping(@NonNull String serverName) {
        return Promise.fromCompletableFuture(
                OumLib.proxy().getServer(serverName)
                        .map(server -> server.ping()
                                .thenApply(ping -> {
                                    String motd = ping.getDescriptionComponent().toString();
                                    String ver = ping.getVersion() != null ? ping.getVersion().getName() : null;
                                    int online = ping.getPlayers().isPresent() ? ping.getPlayers().get().getOnline() : 0;
                                    int max = ping.getPlayers().isPresent() ? ping.getPlayers().get().getMax() : 0;
                                    return new ServerPingResult(true, online, max, motd, ver);
                                })
                                .exceptionally(err -> new ServerPingResult(false, 0, 0, null, null)))
                        .orElseGet(() -> CompletableFuture.completedFuture(new ServerPingResult(false, 0, 0, null, null)))
        );
    }

    @CheckReturnValue
    public static @NonNull Optional<Player> getPlayer(@NonNull String name) {
        return OumLib.proxy().getPlayer(name);
    }

    @CheckReturnValue
    public static @NonNull Optional<Player> getPlayer(@NonNull UUID uuid) {
        return OumLib.proxy().getPlayer(uuid);
    }

    @CheckReturnValue
    public static @NonNull Collection<Player> getPlayers() {
        return OumLib.proxy().getAllPlayers();
    }

    @CheckReturnValue
    public static @NonNull Collection<Player> getPlayersOn(@NonNull String serverName) {
        return OumLib.proxy().getServer(serverName)
                .map(RegisteredServer::getPlayersConnected)
                .orElse(List.of());
    }

    public record ServerPingResult(
            boolean online,
            int currentPlayers,
            int maxPlayers,
            @Nullable String motd,
            @Nullable String version
    ) {
    }
}
