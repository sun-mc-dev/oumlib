package dev.oum.oumlib.entity.display;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public interface VirtualDisplay {

    int getEntityId();

    @NonNull UUID getUniqueId();

    @NonNull Location getLocation();

    void setLocation(@NonNull Location location);

    void teleport(@NonNull Location location);

    double getViewDistance();

    void setViewDistance(double viewDistance);

    @NonNull Billboard getBillboard();

    void setBillboard(@NonNull Billboard billboard);

    @NonNull Vector getScale();

    void setScale(@NonNull Vector scale);

    @NonNull Vector getTranslation();

    void setTranslation(@NonNull Vector translation);

    int getInterpolationDuration();

    void setInterpolationDuration(int ticks);

    int getInterpolationDelay();

    void setInterpolationDelay(int ticks);

    void setVisibilityFilter(@Nullable Predicate<Player> filter);

    boolean isVisibleTo(@NonNull Player player);

    void spawn(@NonNull Player player);

    void destroy(@NonNull Player player);

    void spawnAll(@NonNull Collection<? extends Player> players);

    void destroyAll(@NonNull Collection<? extends Player> players);

    void updateMetadata();

    void updateMetadata(@NonNull Player player);

    @NonNull Set<UUID> getViewers();

    enum Billboard {
        FIXED((byte) 0),
        VERTICAL((byte) 1),
        HORIZONTAL((byte) 2),
        CENTER((byte) 3);

        private final byte id;

        Billboard(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }
    }
}
