package dev.oum.oumlib.effect;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public final class Effects {

    private Effects() {
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull SoundBuilder sound(@NonNull Sound sound) {
        return new SoundBuilder(sound);
    }

    @Contract("_ -> new")
    @CheckReturnValue
    public static @NonNull ParticleBuilder particle(@NonNull Particle particle) {
        return new ParticleBuilder(particle);
    }
}
