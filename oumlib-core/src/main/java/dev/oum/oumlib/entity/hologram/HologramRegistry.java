package dev.oum.oumlib.entity.hologram;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import dev.oum.oumlib.entity.display.VirtualDisplay;
import dev.oum.oumlib.scheduler.Scheduler;
import dev.oum.oumlib.scheduler.TaskHandle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class HologramRegistry extends PacketListenerAbstract {

    private final Set<Hologram> holograms = ConcurrentHashMap.newKeySet();
    private TaskHandle distanceTask;
    private boolean listening = false;

    public HologramRegistry(@NonNull Plugin plugin) {
        super(PacketListenerPriority.NORMAL);
    }

    public void start() {
        if (!listening) {
            try {
                PacketEvents.getAPI().getEventManager().registerListener(this);
            } catch (Throwable ignored) {
            }
            listening = true;
        }
        if (distanceTask == null || distanceTask.isCancelled()) {
            distanceTask = Scheduler.runRepeating(Duration.ZERO, Duration.ofMillis(500), this::tickDistances);
        }
    }

    public void stop() {
        if (distanceTask != null) {
            distanceTask.cancel();
            distanceTask = null;
        }
        if (listening) {
            try {
                PacketEvents.getAPI().getEventManager().unregisterListener(this);
            } catch (Throwable ignored) {
            }
            listening = false;
        }
        for (Hologram h : holograms) {
            h.destroyAll();
        }
        holograms.clear();
    }

    public void register(@NonNull Hologram hologram) {
        holograms.add(hologram);
        for (Player p : Bukkit.getOnlinePlayers()) {
            hologram.spawn(p);
        }
    }

    public void unregister(@NonNull Hologram hologram) {
        holograms.remove(hologram);
        hologram.destroyAll();
    }

    @Contract(pure = true)
    public @NonNull @UnmodifiableView Set<Hologram> getAll() {
        return Collections.unmodifiableSet(holograms);
    }

    private void tickDistances() {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        for (Hologram h : holograms) {
            for (Player p : online) {
                for (HologramLine line : h.getLines()) {
                    VirtualDisplay display = line.getDisplay();
                    boolean visible = display.isVisibleTo(p);
                    boolean viewing = display.getViewers().contains(p.getUniqueId());

                    if (visible && !viewing) {
                        display.spawn(p);
                    } else if (!visible && viewing) {
                        display.destroy(p);
                    }
                }
            }
        }
    }

    @Override
    public void onPacketReceive(@NonNull PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
            return;
        }
        WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
        int targetEntityId = packet.getEntityId();
        Object playerObj = event.getPlayer();
        if (!(playerObj instanceof Player player)) {
            return;
        }

        for (Hologram h : holograms) {
            int lineIndex = h.getLineIndexByEntityId(targetEntityId);
            if (lineIndex != -1) {
                Hologram.ClickListener listener = h.getClickListener();
                if (listener != null) {
                    Hologram.ClickType type = (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK)
                            ? Hologram.ClickType.LEFT_CLICK
                            : Hologram.ClickType.RIGHT_CLICK;
                    Scheduler.run(() -> listener.onClick(player, h, lineIndex, type));
                }
                break;
            }
        }
    }
}
