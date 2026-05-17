package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

@EditClass("net/minecraft/world/entity/player/Player")
public class PlayerInjection {
    @Inject(method = "attack")
    public static void attackInjection(InjectionHelper helper) {
        Player player = (Player) helper.getSelf();
        if (player instanceof LocalPlayer) return;
        if (PlayerAuthData.exists((ServerPlayer) player)) {
            helper.setCancelled(true);
        }
    }
}
