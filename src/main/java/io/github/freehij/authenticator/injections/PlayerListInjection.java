package io.github.freehij.authenticator.injections;

import com.google.common.net.InetAddresses;
import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.authenticator.util.Sessions;
import io.github.freehij.authenticator.data.Messages;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.constant.At;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.github.freehij.authenticator.util.Utils;

@SuppressWarnings("deprecation")
@EditClass("net/minecraft/server/players/PlayerList")
public class PlayerListInjection {
    @Inject(method = "placeNewPlayer")
    public static void placeNewPlayerInjection(InjectionHelper helper) {
        Connection connection = ((Connection) helper.getArg(1));
        if (Utils.isLocal(connection)) return;
        ServerPlayer player = (ServerPlayer) helper.getArg(2);
        String IP = getIpAddress(connection.getRemoteAddress());
        if (IP != null && Sessions.checkSession(player.getName().getString(), IP)) return;
        PlayerAuthData.createNew(player);
    }

    @Inject(method = "placeNewPlayer", at = At.RETURN)
    public static void placeNewPlayerInjection2(InjectionHelper helper) {
        if (Utils.isLocal(((Connection) helper.getArg(1)))) return;
        ServerPlayer player = (ServerPlayer) helper.getArg(2);
        if (!PlayerAuthData.exists(player))
            player.sendSystemMessage(Component.literal(Messages.sessionLogin));
    }

    static String getIpAddress(SocketAddress remoteAddress) {
        if (remoteAddress instanceof InetSocketAddress ipSocketAddress) {
            return InetAddresses.toAddrString(ipSocketAddress.getAddress());
        } else {
            return null;
        }
    }
}
