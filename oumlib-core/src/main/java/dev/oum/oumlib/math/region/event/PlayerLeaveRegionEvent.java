package dev.oum.oumlib.math.region.event;

import dev.oum.oumlib.math.region.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class PlayerLeaveRegionEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String regionId;
    private final Region region;
    private final Location from;
    private final Location to;
    private boolean cancelled = false;

    public PlayerLeaveRegionEvent(@NonNull Player player, @NonNull String regionId, @NonNull Region region, @NonNull Location from, @NonNull Location to) {
        this.player = player;
        this.regionId = regionId;
        this.region = region;
        this.from = from;
        this.to = to;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public @NonNull Player getPlayer() {
        return player;
    }

    public @NonNull String getRegionId() {
        return regionId;
    }

    public @NonNull Region getRegion() {
        return region;
    }

    public @NonNull Location getFrom() {
        return from;
    }

    public @NonNull Location getTo() {
        return to;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
