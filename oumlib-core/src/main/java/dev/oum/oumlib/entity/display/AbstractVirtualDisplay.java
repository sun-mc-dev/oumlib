package dev.oum.oumlib.entity.display;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public abstract class AbstractVirtualDisplay implements VirtualDisplay {

    private static final AtomicInteger ENTITY_ID_COUNTER = new AtomicInteger(1_500_000_000);

    protected final int entityId;
    protected final UUID uniqueId;
    protected final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    protected Location location;
    protected double viewDistance = 48.0;
    protected Billboard billboard = Billboard.CENTER;
    protected Vector scale = new Vector(1.0, 1.0, 1.0);
    protected Vector translation = new Vector(0.0, 0.0, 0.0);
    protected int interpolationDuration = 0;
    protected int interpolationDelay = 0;
    protected Predicate<Player> visibilityFilter;

    protected AbstractVirtualDisplay(@NonNull Location location) {
        this.entityId = ENTITY_ID_COUNTER.incrementAndGet();
        this.uniqueId = UUID.randomUUID();
        this.location = location.clone();
    }

    protected abstract @NonNull EntityType getEntityType();

    protected abstract void appendCustomMetadata(@NonNull Player player, @NonNull List<EntityData<?>> list);

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public @NonNull UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public @NonNull Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(@NonNull Location location) {
        this.location = location.clone();
    }

    @Override
    public void teleport(@NonNull Location location) {
        this.location = location.clone();
        if (viewers.isEmpty()) return;
        com.github.retrooper.packetevents.protocol.world.Location peLoc =
                new com.github.retrooper.packetevents.protocol.world.Location(
                        location.getX(), location.getY(), location.getZ(),
                        location.getYaw(), location.getPitch());
        WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(entityId, peLoc, false);
        for (UUID uid : viewers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) {
                sendPacket(p, packet);
            }
        }
    }

    @Override
    public double getViewDistance() {
        return viewDistance;
    }

    @Override
    public void setViewDistance(double viewDistance) {
        this.viewDistance = viewDistance;
    }

    @Override
    public @NonNull Billboard getBillboard() {
        return billboard;
    }

    @Override
    public void setBillboard(@NonNull Billboard billboard) {
        this.billboard = billboard;
    }

    @Override
    public @NonNull Vector getScale() {
        return scale.clone();
    }

    @Override
    public void setScale(@NonNull Vector scale) {
        this.scale = scale.clone();
    }

    @Override
    public @NonNull Vector getTranslation() {
        return translation.clone();
    }

    @Override
    public void setTranslation(@NonNull Vector translation) {
        this.translation = translation.clone();
    }

    @Override
    public int getInterpolationDuration() {
        return interpolationDuration;
    }

    @Override
    public void setInterpolationDuration(int ticks) {
        this.interpolationDuration = ticks;
    }

    @Override
    public int getInterpolationDelay() {
        return interpolationDelay;
    }

    @Override
    public void setInterpolationDelay(int ticks) {
        this.interpolationDelay = ticks;
    }

    @Override
    public void setVisibilityFilter(@Nullable Predicate<Player> filter) {
        this.visibilityFilter = filter;
    }

    @Override
    public boolean isVisibleTo(@NonNull Player player) {
        if (!player.isOnline()) return false;
        if (location.getWorld() != null && !location.getWorld().equals(player.getWorld())) return false;
        if (location.distanceSquared(player.getLocation()) > (viewDistance * viewDistance)) return false;
        return visibilityFilter == null || visibilityFilter.test(player);
    }

    @Override
    public void spawn(@NonNull Player player) {
        if (!isVisibleTo(player)) return;
        com.github.retrooper.packetevents.protocol.world.Location peLoc =
                new com.github.retrooper.packetevents.protocol.world.Location(
                        location.getX(), location.getY(), location.getZ(),
                        location.getYaw(), location.getPitch());
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                entityId,
                uniqueId,
                getEntityType(),
                peLoc,
                location.getYaw(),
                0,
                new Vector3d(0, 0, 0)
        );
        sendPacket(player, spawnPacket);
        viewers.add(player.getUniqueId());
        updateMetadata(player);
    }

    @Override
    public void destroy(@NonNull Player player) {
        viewers.remove(player.getUniqueId());
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityId);
        sendPacket(player, destroyPacket);
    }

    @Override
    public void spawnAll(@NonNull Collection<? extends Player> players) {
        for (Player player : players) {
            spawn(player);
        }
    }

    @Override
    public void destroyAll(@NonNull Collection<? extends Player> players) {
        for (Player player : players) {
            destroy(player);
        }
    }

    @Override
    public void updateMetadata() {
        for (UUID uid : viewers) {
            Player p = Bukkit.getPlayer(uid);
            if (p != null && p.isOnline()) {
                updateMetadata(p);
            }
        }
    }

    @Override
    public void updateMetadata(@NonNull Player player) {
        List<EntityData<?>> dataList = new ArrayList<>();
        dataList.add(new EntityData<>(8, EntityDataTypes.INT, interpolationDelay));
        dataList.add(new EntityData<>(9, EntityDataTypes.INT, interpolationDuration));
        dataList.add(new EntityData<>(10, EntityDataTypes.INT, interpolationDuration));
        dataList.add(new EntityData<>(11, EntityDataTypes.VECTOR3F, new Vector3f((float) translation.getX(), (float) translation.getY(), (float) translation.getZ())));
        dataList.add(new EntityData<>(12, EntityDataTypes.VECTOR3F, new Vector3f((float) scale.getX(), (float) scale.getY(), (float) scale.getZ())));
        dataList.add(new EntityData<>(13, EntityDataTypes.QUATERNION, new Quaternion4f(0, 0, 0, 1)));
        dataList.add(new EntityData<>(14, EntityDataTypes.QUATERNION, new Quaternion4f(0, 0, 0, 1)));
        dataList.add(new EntityData<>(15, EntityDataTypes.BYTE, billboard.getId()));

        appendCustomMetadata(player, dataList);

        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(entityId, dataList);
        sendPacket(player, metadataPacket);
    }

    @Override
    public @NonNull Set<UUID> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    protected void sendPacket(@NonNull Player player, @NonNull Object packet) {
        try {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, (PacketWrapper<?>) packet);
        } catch (Throwable ignored) {
        }
    }
}
