package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.player.PlayerAuthData;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("deprecation")
@EditClass("net/minecraft/server/level/ServerPlayerGameMode")
public class ServerPlayerGameModeInjection {
    @Inject(method = "handleBlockBreakAction")
    public static void destroyInjection(InjectionHelper helper) {
        if (PlayerAuthData.exists((ServerPlayer) helper.getReflector().getField("player").get())) {
            BlockPos pos = (BlockPos) helper.getArg(1);
            pos.offset(0, 32767, 0);
            helper.setCancelled(true);
        }
    }
}
