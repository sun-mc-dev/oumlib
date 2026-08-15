package dev.oum.oumlib.entity.hologram;

import dev.oum.oumlib.entity.display.VirtualDisplay;
import org.bukkit.Location;
import org.jspecify.annotations.NonNull;

public interface HologramLine {

    @NonNull VirtualDisplay getDisplay();

    double getHeightOffset();

    void setLocation(@NonNull Location location);

    void teleport(@NonNull Location location);
}
