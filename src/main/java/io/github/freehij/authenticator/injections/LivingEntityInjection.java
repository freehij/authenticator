package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.server.level.ServerPlayer;

@EditClass("net/minecraft/world/entity/LivingEntity")
public class LivingEntityInjection {
    @Inject(method = "addEffect")
    public static void addEffectInjection(InjectionHelper helper) {
        if (helper.getSelf() instanceof ServerPlayer player) {
            if (PlayerAuthData.exists(player)) {
                helper.setReturnValue(false);
                helper.setCancelled(true);
            }
        }
    }
}
