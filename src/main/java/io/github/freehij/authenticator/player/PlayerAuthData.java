package io.github.freehij.authenticator.player;

import io.github.freehij.authenticator.value.Values;
import io.github.freehij.loader.util.Reflector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;

@SuppressWarnings("unchecked")
public class PlayerAuthData {
    static final Map<ServerPlayer, Entry> map = new HashMap<>();

    static class Entry {
        int attempts;
        final PlayerData playerData;

        Entry(int attempts, PlayerData playerData) {
            this.attempts = attempts;
            this.playerData = playerData;
        }
    }

    public static class PlayerData {
        final ServerPlayer player;
        final double x, y, z;
        final float pitch, yaw, health;
        final ItemStack[] mainInvItems;
        final EnumMap<EquipmentSlot, ItemStack> equipmentInvItems = new EnumMap<>(EquipmentSlot.class);

        PlayerData(ServerPlayer player) {
            this.player = player;
            x = player.getX();
            y = player.getY();
            z = player.getZ();
            pitch = player.getYRot();
            yaw = player.getXRot();
            health = player.getHealth();
            Reflector invReflector = new Reflector(Inventory.class, player.getInventory());
            mainInvItems = ((List<ItemStack>) invReflector.getField("items").get()).toArray(new ItemStack[0]);
            equipmentInvItems.putAll(((EnumMap<EquipmentSlot, ItemStack>) invReflector.getField("equipment")
                    .getField("items").get()));
        }

        public void restore() {
            player.connection.teleport(x, y, z, pitch, yaw);
            player.setHealth(health);
            Reflector invReflector = new Reflector(Inventory.class, player.getInventory());
            List<ItemStack> mainInv = (List<ItemStack>) invReflector.getField("items").get();
            for (int i = 0; i < 36; i++) {
                mainInv.set(i, mainInvItems[i]);
                player.connection.send(player.getInventory().createInventoryUpdatePacket(i));
            }
            EnumMap<EquipmentSlot, ItemStack> equipmentInv = ((EnumMap<EquipmentSlot, ItemStack>) invReflector
                    .getField("equipment").getField("items").get());
            equipmentInv.putAll(equipmentInvItems);
        }
    }

    public static boolean exists(ServerPlayer player) {
        return map.containsKey(player);
    }

    public static boolean increaseAttempts(ServerPlayer player) {
        map.get(player).attempts++;
        return map.get(player).attempts >= Values.maxLoginAttempts;
    }

    public static PlayerData getPlayerData(ServerPlayer player) {
        return map.get(player).playerData;
    }

    public static void createNew(ServerPlayer player) {
        map.put(player, new Entry(0, new PlayerData(player)));
        if (player.isDeadOrDying()) player.setHealth(20);
        player.setPos(0, 65, 0);
        player.setYRot(0);
        player.setXRot(0);
        player.getInventory().clearContent();
    }

    public static void removeSafe(ServerPlayer player) {
        if (!exists(player)) return;
        getPlayerData(player).restore();
        map.remove(player);
    }
}