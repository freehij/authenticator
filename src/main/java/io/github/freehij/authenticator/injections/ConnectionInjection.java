package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import io.github.freehij.loader.util.Reflector;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@EditClass("net/minecraft/network/Connection")
public class ConnectionInjection {
    @Inject(method = "disconnect", descriptor = "(Lnet/minecraft/network/DisconnectionDetails;)V")
    public static void disconnect(InjectionHelper helper) {
        ServerPlayer player = getPlayer((Connection) helper.getSelf());
        if (player == null) return;
        PlayerAuthData.removeSafe(player);
    }

    static ServerPlayer getPlayer(Connection connection) {
        for (ServerPlayer player : Authenticator.server.getPlayerList().getPlayers()) {
            if (new Reflector(ServerCommonPacketListenerImpl.class, player.connection).getField("connection").get()
                    == connection) {
                return player;
            }
        }
        return null;
    }
}
