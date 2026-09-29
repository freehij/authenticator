package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.authenticator.data.Messages;
import io.github.freehij.loader.annotation.*;
import io.github.freehij.loader.constant.At;
import io.github.freehij.loader.constant.Shift;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@SuppressWarnings("deprecation")
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
        ServerGamePacketListenerImpl connection = ((ServerGamePacketListenerImpl) helper.getSelf());
        if (PlayerAuthData.exists(connection.player)) connection.resetFlyingTicks();
    }

    @Inject(method = "handleMovePlayer", at = At.NONE,
            advancedAt = @AdvancedAt(at = AdvancedAt.At.INVOKE,
                    optional = "net/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread",
                    shift = Shift.AFTER))
    public static void handleMovePlayerInjection(InjectionHelper helper) {
        ServerPlayer player = ((ServerGamePacketListenerImpl) helper.getSelf()).player;
        if (PlayerAuthData.exists(player)) helper.setCancelled(true);
    }
}
