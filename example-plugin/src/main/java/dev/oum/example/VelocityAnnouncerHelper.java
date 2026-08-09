package dev.oum.example;

import com.velocitypowered.api.event.connection.PostLoginEvent;
import dev.oum.oumlib.OumLib;
import dev.oum.oumlib.event.Events;
import dev.oum.oumlib.text.Text;
import net.kyori.adventure.text.Component;

public final class VelocityAnnouncerHelper {

    private VelocityAnnouncerHelper() {
    }

    public static void sendActionbarToAll(String actionbarMessage) {
        OumLib.proxy().getAllPlayers().forEach(player ->
                Text.actionBar(player, actionbarMessage)
        );
    }

    public static void registerOnceListener(String message) {
        Events.listenOnce(PostLoginEvent.class, event ->
                Text.Preset.info(event.getPlayer(), "<yellow>Special Notice: </yellow>" + message)
        );
    }

    public static void broadcast(Component component) {
        OumLib.proxy().sendMessage(component);
    }
}
