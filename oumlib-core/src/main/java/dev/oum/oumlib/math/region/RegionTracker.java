package dev.oum.oumlib.math.region;

import dev.oum.oumlib.math.region.event.PlayerEnterRegionEvent;
import dev.oum.oumlib.math.region.event.PlayerLeaveRegionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class RegionTracker implements Listener {

    private final Map<String, Region> regions = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> chunkGrid = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> playerRegions = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private boolean listening = false;

    public RegionTracker(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!listening) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            listening = true;
        }
    }

    public void stop() {
        if (listening) {
            HandlerList.unregisterAll(this);
            listening = false;
        }
        playerRegions.clear();
    }

    public synchronized void register(@NonNull String id, @NonNull Region region) {
        regions.put(id, region);
        for (long chunkKey : region.getIntersectingChunkKeys()) {
            chunkGrid.computeIfAbsent(chunkKey, k -> ConcurrentHashMap.newKeySet()).add(id);
        }
    }

    public synchronized @Nullable Region unregister(@NonNull String id) {
        Region removed = regions.remove(id);
        if (removed != null) {
            for (long chunkKey : removed.getIntersectingChunkKeys()) {
                Set<String> set = chunkGrid.get(chunkKey);
                if (set != null) {
                    set.remove(id);
                    if (set.isEmpty()) {
                        chunkGrid.remove(chunkKey);
                    }
                }
            }
            playerRegions.values().forEach(set -> set.remove(id));
        }
        return removed;
    }

    public @NonNull Optional<Region> get(@NonNull String id) {
        return Optional.ofNullable(regions.get(id));
    }

    public @NonNull Map<String, Region> getAll() {
        return Collections.unmodifiableMap(regions);
    }

    public @NonNull Set<String> getRegionsAt(@NonNull Location loc) {
        int cx = loc.getBlockX() >> 4;
        int cz = loc.getBlockZ() >> 4;
        long chunkKey = Chunk.getChunkKey(cx, cz);
        Set<String> candidates = chunkGrid.get(chunkKey);
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> inside = new HashSet<>();
        for (String id : candidates) {
            Region r = regions.get(id);
            if (r != null && r.contains(loc)) {
                inside.add(id);
            }
        }
        return inside;
    }

    public @NonNull Set<String> getPlayerRegions(@NonNull Player player) {
        Set<String> set = playerRegions.get(player.getUniqueId());
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }

    public boolean isInside(@NonNull Player player, @NonNull String regionId) {
        Set<String> set = playerRegions.get(player.getUniqueId());
        return set != null && set.contains(regionId);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        handleMovement(event.getPlayer(), from, to);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        handleMovement(event.getPlayer(), from, to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Set<String> inside = getRegionsAt(player.getLocation());
        playerRegions.put(player.getUniqueId(), ConcurrentHashMap.newKeySet());
        Set<String> current = playerRegions.get(player.getUniqueId());
        for (String id : inside) {
            Region r = regions.get(id);
            if (r != null) {
                PlayerEnterRegionEvent enterEvent = new PlayerEnterRegionEvent(player, id, r, player.getLocation(), player.getLocation());
                Bukkit.getPluginManager().callEvent(enterEvent);
                if (!enterEvent.isCancelled()) {
                    current.add(id);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uid = event.getPlayer().getUniqueId();
        playerRegions.remove(uid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location to = event.getRespawnLocation();
        handleMovement(player, player.getLocation(), to);
    }

    private void handleMovement(Player player, Location from, Location to) {
        Set<String> current = playerRegions.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        Set<String> toRegions = getRegionsAt(to);

        for (String id : new HashSet<>(current)) {
            if (!toRegions.contains(id)) {
                Region r = regions.get(id);
                if (r != null) {
                    PlayerLeaveRegionEvent leaveEvent = new PlayerLeaveRegionEvent(player, id, r, from, to);
                    Bukkit.getPluginManager().callEvent(leaveEvent);
                    if (leaveEvent.isCancelled()) {
                        player.teleport(from);
                        return;
                    }
                }
                current.remove(id);
            }
        }

        for (String id : toRegions) {
            if (!current.contains(id)) {
                Region r = regions.get(id);
                if (r != null) {
                    PlayerEnterRegionEvent enterEvent = new PlayerEnterRegionEvent(player, id, r, from, to);
                    Bukkit.getPluginManager().callEvent(enterEvent);
                    if (enterEvent.isCancelled()) {
                        player.teleport(from);
                        return;
                    }
                    current.add(id);
                }
            }
        }
    }

    public void clear() {
        regions.clear();
        chunkGrid.clear();
        playerRegions.clear();
    }
}
