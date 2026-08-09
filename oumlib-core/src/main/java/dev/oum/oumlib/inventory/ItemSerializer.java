package dev.oum.oumlib.inventory;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Base64;

public final class ItemSerializer {

    private ItemSerializer() {
    }

    public static @NonNull String serialize(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) return "";
        try {
            byte[] bytes = item.serializeAsBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack", e);
        }
    }

    public static @Nullable ItemStack deserialize(@NonNull String base64) {
        if (base64.isEmpty()) return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return ItemStack.deserializeBytes(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack", e);
        }
    }

    public static @NonNull String serializeArray(ItemStack @NonNull [] items) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeInt(items.length);
            for (ItemStack item : items) {
                if (item == null || item.isEmpty()) {
                    dos.writeInt(0);
                } else {
                    byte[] bytes = item.serializeAsBytes();
                    dos.writeInt(bytes.length);
                    dos.write(bytes);
                }
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack array", e);
        }
    }

    public static ItemStack @NonNull [] deserializeArray(@NonNull String base64) {
        if (base64.isEmpty()) return new ItemStack[0];
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             DataInputStream dis = new DataInputStream(bais)) {
            int length = dis.readInt();
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                int size = dis.readInt();
                if (size == 0) {
                    items[i] = null;
                } else {
                    byte[] bytes = new byte[size];
                    dis.readFully(bytes);
                    items[i] = ItemStack.deserializeBytes(bytes);
                }
            }
            return items;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack array", e);
        }
    }
}
