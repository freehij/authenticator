package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.player.PlayerAuthData;
import io.github.freehij.authenticator.value.Messages;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import io.github.freehij.loader.util.Reflector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@EditClass("net/minecraft/server/network/ServerGamePacketListenerImpl")
public class ServerGamePacketListenerImplInjection {
    @Inject(method = "handleChat")
    public static void handleChatInjection(InjectionHelper helper) {
        ServerPlayer player = ((ServerGamePacketListenerImpl) helper.getSelf()).player;
        if (PlayerAuthData.exists(player)) {
            player.sendSystemMessage(Component.literal(Messages.notLoggedIn));
            helper.setCancelled(true);
        }
    }

    @Inject(method = "tickPlayer")
    public static void tickPlayerInjection(InjectionHelper helper) {
        if (PlayerAuthData.exists(((ServerGamePacketListenerImpl) helper.getSelf()).player)) {
            Reflector reflector = helper.getReflector();
            reflector.setField("aboveGroundTickCount", 0);
            reflector.setField("aboveGroundVehicleTickCount", 0);
        }
    }
}
