package dev.oum.oumlib.effect;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public final class SoundBuilder {
    private final Key soundKey;
    private Source source = Source.MASTER;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private float pitchRange = 0.0f;

    public SoundBuilder(@NonNull Key soundKey) {
        this.soundKey = soundKey;
    }

    public SoundBuilder(net.kyori.adventure.sound.Sound.@NonNull Type soundType) {
        this.soundKey = soundType.key();
    }

    public SoundBuilder(org.bukkit.@NonNull Sound sound) {
        this.soundKey = Registry.SOUNDS.getKey(sound);
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SoundBuilder source(@NonNull Source source) {
        this.source = source;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SoundBuilder category(@NonNull SoundCategory category) {
        try {
            this.source = Source.valueOf(category.name());
        } catch (IllegalArgumentException e) {
            this.source = Source.MASTER;
        }
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SoundBuilder volume(float volume) {
        this.volume = volume;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SoundBuilder pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public @NonNull SoundBuilder pitchVariance(float range) {
        this.pitchRange = range;
        return this;
    }

    private float calculatePitch() {
        if (pitchRange <= 0.0f) return pitch;
        float min = pitch - pitchRange;
        float max = pitch + pitchRange;
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }

    @Contract("-> new")
    @CheckReturnValue
    public net.kyori.adventure.sound.@NonNull Sound build() {
        return net.kyori.adventure.sound.Sound.sound(soundKey, source, volume, calculatePitch());
    }

    public void play(@NonNull Audience audience) {
        audience.playSound(build());
    }

    public void play(@NonNull Audience audience, double x, double y, double z) {
        audience.playSound(build(), x, y, z);
    }

    public void play(@NonNull Location location) {
        if (location.getWorld() != null) {
            SoundCategory cat;
            try {
                cat = SoundCategory.valueOf(source.name());
            } catch (Exception e) {
                cat = SoundCategory.MASTER;
            }
            location.getWorld().playSound(location, soundKey.asString(), cat, volume, calculatePitch());
        }
    }

    public void play(@NonNull Player player) {
        play((Audience) player);
    }

    public void play(@NonNull Player player, @NonNull Location location) {
        if (player.getWorld().equals(location.getWorld())) {
            SoundCategory cat;
            try {
                cat = SoundCategory.valueOf(source.name());
            } catch (Exception e) {
                cat = SoundCategory.MASTER;
            }
            player.playSound(location, soundKey.asString(), cat, volume, calculatePitch());
        }
    }

    public void play(@NonNull Collection<? extends Player> players, @NonNull Location location) {
        float finalPitch = calculatePitch();
        SoundCategory cat;
        try {
            cat = SoundCategory.valueOf(source.name());
        } catch (Exception e) {
            cat = SoundCategory.MASTER;
        }
        for (Player p : players) {
            if (p.getWorld().equals(location.getWorld())) {
                p.playSound(location, soundKey.asString(), cat, volume, finalPitch);
            }
        }
    }
}
