package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.authenticator.data.Messages;
import io.github.freehij.authenticator.data.Values;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

@EditClass("net/minecraft/server/level/ServerPlayer")
public class ServerPlayerInjection {
    @Inject(method = "disconnect")
    public static void disconnectInjection(InjectionHelper helper) {
        PlayerAuthData.removeSafe((ServerPlayer) helper.getSelf());
    }

    @Inject(method = "doTick")
    public static void tickInjection(InjectionHelper helper) {
        ServerPlayer player = (ServerPlayer) helper.getSelf();
        if (PlayerAuthData.exists(player)) {
            helper.setCancelled(true);
            player.fallDistance = 0;
            if (player.tickCount % 5 == 0) player.connection.teleport(0, 65, 0, 0, 0);
            if (player.tickCount % 80 == 0 || player.tickCount == 1) {
                player.sendSystemMessage(Component.literal(Authenticator.database.isRegistered(player.getName()
                        .getString()) ? Messages.login : Messages.register));
            }
            if (player.tickCount == Values.loginTimeOut && PlayerAuthData.exists(player)) {
                player.connection.disconnect(Component.literal(Messages.tookTooLongToLogin));
            }
        }
    }

    @Inject(method = "hurtServer")
    public static void hurtServerInjection(InjectionHelper helper) {
        if (PlayerAuthData.exists((ServerPlayer) helper.getSelf())) {
            helper.setReturnValue(false);
            helper.setCancelled(true);
        }
    }
}
