package dev.oum.oumlib.bridge.permission;

import dev.oum.oumlib.OumLib;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class Permission {

    private final String name;
    private final String description;
    private final Default defaultValue;

    private Permission(@NonNull Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.defaultValue = builder.defaultValue;
        registerOnPaper();
    }

    public static @NonNull Builder builder(@NonNull String name) {
        return new Builder(name);
    }

    public static @NonNull Permission of(@NonNull String name) {
        return builder(name).build();
    }

    public @NonNull String name() {
        return name;
    }

    public @Nullable String description() {
        return description;
    }

    public @NonNull Default defaultValue() {
        return defaultValue;
    }

    public boolean has(@NonNull Audience audience) {
        try {
            Class<?> permissibleClass = Class.forName("org.bukkit.permissions.Permissible");
            if (permissibleClass.isInstance(audience)) {
                boolean isOp = (boolean) permissibleClass.getMethod("isOp").invoke(audience);
                if (isOp) return true;
                return (boolean) permissibleClass.getMethod("hasPermission", String.class).invoke(audience, name);
            }
        } catch (Exception ignored) {
        }

        try {
            Class<?> subjectClass = Class.forName("com.velocitypowered.api.permission.PermissionSubject");
            if (subjectClass.isInstance(audience)) {
                return (boolean) subjectClass.getMethod("hasPermission", String.class).invoke(audience, name);
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    private void registerOnPaper() {
        try {
            Class.forName("org.bukkit.permissions.Permission");
            Class.forName("dev.oum.oumlib.bridge.permission.PaperPermissionHelper")
                    .getMethod("register", String.class, String.class, Default.class)
                    .invoke(null, name, description, defaultValue);
        } catch (ClassNotFoundException ignored) {
        } catch (Exception e) {
            OumLib.logError("Failed to register Bukkit permission: " + name, e);
        }
    }

    public enum Default {
        TRUE, FALSE, OP, NOT_OP
    }

    public static final class Builder {
        private final String name;
        private String description = "";
        private Default defaultValue = Default.OP;

        private Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder defaultValue(Default defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        @Contract(" -> new")
        public @NonNull Permission build() {
            return new Permission(this);
        }
    }
}
