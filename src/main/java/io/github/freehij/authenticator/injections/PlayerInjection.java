package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.server.level.ServerPlayer;

@EditClass("net/minecraft/world/entity/player/Player")
public class PlayerInjection {
    @Inject(method = "attack")
    public static void attackInjection(InjectionHelper helper) {
        if (PlayerAuthData.exists((ServerPlayer) helper.getSelf())) {
            helper.setCancelled(true);
        }
    }
}
