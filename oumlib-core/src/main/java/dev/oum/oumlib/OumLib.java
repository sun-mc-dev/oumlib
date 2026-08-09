package dev.oum.oumlib;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.oum.oumlib.command.Argument;
import dev.oum.oumlib.command.CommandContext;
import dev.oum.oumlib.config.ConfigWatcher;
import dev.oum.oumlib.entity.hologram.HologramRegistry;
import dev.oum.oumlib.event.EventBus;
import dev.oum.oumlib.event.platform.PaperEventBus;
import dev.oum.oumlib.event.platform.VelocityEventBus;
import dev.oum.oumlib.inventory.MenuRegistry;
import dev.oum.oumlib.inventory.recipe.RecipeRegistry;
import dev.oum.oumlib.math.region.RegionTracker;
import dev.oum.oumlib.pdc.metadata.VolatileData;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.platform.BukkitSchedulerAdapter;
import dev.oum.oumlib.scheduler.platform.VelocitySchedulerAdapter;
import dev.oum.oumlib.text.Preset;
import dev.oum.oumlib.text.PresetRegistry;
import dev.oum.oumlib.text.placeholder.PlaceholderRegistry;
import dev.oum.oumlib.text.placeholder.bridge.MiniPlaceholdersHelper;
import dev.oum.oumlib.text.placeholder.bridge.PapiHelper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OumLib {

    private static Plugin plugin;
    private static ProxyServer proxyServer;
    private static Object velocityPlugin;

    private static PresetRegistry presetRegistry;
    private static PlaceholderRegistry placeholderRegistry;
    private static HologramRegistry hologramRegistry;
    private static RegionTracker regionTracker;
    private static RecipeRegistry recipeRegistry;
    private static boolean initialized;
    private static boolean debugMode = false;
    private static BiConsumer<CommandContext, Throwable> commandErrorHandler = (context, ex) -> {
        OumLib.logError("Error executing command /" + context.label(), ex);
        context.sender().sendMessage(MiniMessage.miniMessage()
                .deserialize("<red>An unexpected error occurred while executing this command.</red>"));
    };

    private OumLib() {
    }

    @Contract("_ -> new")
    public static @NonNull InitBuilder init(Plugin p) {
        if (initialized) throw new IllegalStateException("OumLib already initialized.");
        plugin = p;
        presetRegistry = new PresetRegistry();
        placeholderRegistry = new PlaceholderRegistry();

        PaperEventBus.initialize(p);
        EventBus.initialize(PaperEventBus.get());

        BukkitSchedulerAdapter.initialize(p);
        Scheduler.initialize(BukkitSchedulerAdapter.get());

        p.getServer().getMessenger().registerIncomingPluginChannel(p, "oumlib:autocomplete",
                (channel, player, message) -> handleSpigotAutocompleteMessage(player, message));
        p.getServer().getMessenger().registerOutgoingPluginChannel(p, "oumlib:autocomplete");

        detectIntegrations(p);
        hologramRegistry = new HologramRegistry(p);
        hologramRegistry.start();
        regionTracker = new RegionTracker(p);
        regionTracker.start();
        recipeRegistry = new RecipeRegistry();
        VolatileData.initialize();
        initialized = true;
        return new InitBuilder();
    }

    @Contract("_, _ -> new")
    public static @NonNull InitBuilder init(ProxyServer server, Object pluginInstance) {
        if (initialized) throw new IllegalStateException("OumLib already initialized.");
        proxyServer = server;
        velocityPlugin = pluginInstance;
        presetRegistry = new PresetRegistry();
        placeholderRegistry = new PlaceholderRegistry();

        VelocityEventBus.initialize(server, pluginInstance);
        EventBus.initialize(VelocityEventBus.get());

        VelocitySchedulerAdapter.initialize(server, pluginInstance);
        Scheduler.initialize(VelocitySchedulerAdapter.get());

        server.getChannelRegistrar().register(MinecraftChannelIdentifier.from("oumlib:autocomplete"));
        server.getEventManager().register(pluginInstance, new Object() {
            @Subscribe
            public void onPluginMessage(PluginMessageEvent event) {
                if (event.getIdentifier().getId().equals("oumlib:autocomplete")) {
                    event.setResult(PluginMessageEvent.ForwardResult.handled());
                    try {
                        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(event.getData()));
                        String uuidStr = dis.readUTF();
                        int size = dis.readInt();
                        List<String> suggestions = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            suggestions.add(dis.readUTF());
                        }
                        Argument.handleVelocityPluginMessage(UUID.fromString(uuidStr), suggestions);
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        detectVelocityIntegrations(server);
        initialized = true;
        return new InitBuilder();
    }

    private static void handleSpigotAutocompleteMessage(Player player, byte[] message) {
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(message));
            String uuidStr = dis.readUTF();
            String queryType = dis.readUTF();
            String remaining = dis.readUTF();

            List<String> suggestions = getSuggestions(queryType, remaining);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(uuidStr);
            dos.writeInt(suggestions.size());
            for (String s : suggestions) {
                dos.writeUTF(s);
            }
            player.sendPluginMessage(plugin, "oumlib:autocomplete", baos.toByteArray());
        } catch (Exception ignored) {
        }
    }

    private static @NonNull List<String> getSuggestions(@NonNull String queryType, String remaining) {
        List<String> suggestions = new ArrayList<>();
        if (queryType.equalsIgnoreCase("worlds")) {
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().toLowerCase(Locale.ROOT).startsWith(remaining.toLowerCase(Locale.ROOT))) {
                    suggestions.add(w.getName());
                }
            }
        } else if (queryType.equalsIgnoreCase("players")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(remaining.toLowerCase(Locale.ROOT))) {
                    suggestions.add(p.getName());
                }
            }
        }
        return suggestions;
    }

    private static void detectIntegrations(@NonNull Plugin p) {
        if (p.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PapiHelper.register(p, placeholderRegistry);
        }
        if (p.getServer().getPluginManager().isPluginEnabled("MiniPlaceholders")) {
            MiniPlaceholdersHelper.register(placeholderRegistry);
        }
    }

    private static void detectVelocityIntegrations(@NonNull ProxyServer server) {
        if (server.getPluginManager().isLoaded("miniplaceholders")) {
            MiniPlaceholdersHelper.register(placeholderRegistry);
        }
    }

    public static void shutdown() {
        Scheduler.shutdownAll();
        MenuRegistry.shutdown();
        ConfigWatcher.shutdown();
        if (hologramRegistry != null) {
            hologramRegistry.stop();
            hologramRegistry = null;
        }
        if (regionTracker != null) {
            regionTracker.stop();
            regionTracker = null;
        }
        if (recipeRegistry != null) {
            recipeRegistry.unregisterAll();
            recipeRegistry = null;
        }
        VolatileData.clearAll();
        plugin = null;
        proxyServer = null;
        velocityPlugin = null;
        presetRegistry = null;
        placeholderRegistry = null;
        initialized = false;
    }

    public static @NonNull Logger logger() {
        return Logger.getLogger("OumLib");
    }

    public static Plugin plugin() {
        assertInit();
        if (plugin == null) throw new IllegalStateException("Plugin is only available on Paper platform.");
        return plugin;
    }

    public static ProxyServer proxy() {
        assertInit();
        if (proxyServer == null) throw new IllegalStateException("ProxyServer is only available on Velocity platform.");
        return proxyServer;
    }

    public static Object velocityPlugin() {
        assertInit();
        if (velocityPlugin == null)
            throw new IllegalStateException("Velocity plugin instance is only available on Velocity platform.");
        return velocityPlugin;
    }

    public static boolean isPaper() {
        return plugin != null;
    }

    public static boolean isVelocity() {
        return proxyServer != null;
    }

    public static HologramRegistry holograms() {
        assertInit();
        if (hologramRegistry == null)
            throw new IllegalStateException("Hologram registry is only available on Paper platform.");
        return hologramRegistry;
    }

    public static RegionTracker regions() {
        assertInit();
        if (regionTracker == null)
            throw new IllegalStateException("Region tracker is only available on Paper platform.");
        return regionTracker;
    }

    public static RecipeRegistry recipes() {
        assertInit();
        if (recipeRegistry == null)
            throw new IllegalStateException("Recipe registry is only available on Paper platform.");
        return recipeRegistry;
    }

    public static @NonNull File getDataFolder() {
        assertInit();
        if (plugin != null) {
            return plugin.getDataFolder();
        } else {
            String id = proxyServer.getPluginManager().fromInstance(velocityPlugin)
                    .map(c -> c.getDescription().getId())
                    .orElse("oumlib-plugin");
            return new File("plugins", id);
        }
    }

    public static boolean isDebug() {
        return debugMode;
    }

    public static void setDebug(boolean enabled) {
        debugMode = enabled;
    }

    public static void logInfo(String message) {
        if (plugin != null) {
            plugin.getSLF4JLogger().info(message);
        } else {
            Logger.getLogger("OumLib").log(Level.INFO, message);
        }
    }

    public static void logWarning(String message) {
        if (plugin != null) {
            plugin.getSLF4JLogger().warn(message);
        } else {
            Logger.getLogger("OumLib").log(Level.WARNING, message);
        }
    }

    public static void logError(String message) {
        if (plugin != null) {
            plugin.getSLF4JLogger().error(message);
        } else {
            Logger.getLogger("OumLib").log(Level.SEVERE, message);
        }
    }

    public static void logError(String message, Throwable t) {
        if (plugin != null) {
            plugin.getSLF4JLogger().error(message, t);
        } else {
            Logger.getLogger("OumLib").log(Level.SEVERE, message, t);
        }
    }

    public static void logDebug(String message) {
        if (!debugMode) return;
        if (plugin != null) {
            plugin.getSLF4JLogger().debug(message);
        } else {
            Logger.getLogger("OumLib").log(Level.INFO, "[DEBUG] " + message);
        }
    }

    public static PresetRegistry presets() {
        assertInit();
        return presetRegistry;
    }

    public static PlaceholderRegistry placeholders(String namespace) {
        assertInit();
        return placeholderRegistry.forNamespace(namespace);
    }

    public static PlaceholderRegistry globalRegistry() {
        return placeholderRegistry;
    }

    public static @NonNull Audience players() {
        assertInit();
        if (plugin != null) {
            return Audience.audience(plugin.getServer().getOnlinePlayers());
        } else {
            return Audience.audience(proxyServer.getAllPlayers());
        }
    }

    public static @NonNull Audience console() {
        assertInit();
        if (plugin != null) {
            return plugin.getServer().getConsoleSender();
        } else {
            return proxyServer.getConsoleCommandSource();
        }
    }

    public static BiConsumer<CommandContext, Throwable> commandErrorHandler() {
        return commandErrorHandler;
    }

    private static void assertInit() {
        if (!initialized) throw new IllegalStateException("Call OumLib.init(plugin) first.");
    }

    public static final class InitBuilder {
        public InitBuilder preset(Preset type, String prefix) {
            presetRegistry.register(type, prefix);
            return this;
        }

        public InitBuilder commandErrorHandler(BiConsumer<CommandContext, Throwable> handler) {
            commandErrorHandler = handler;
            return this;
        }
    }
}