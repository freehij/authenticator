package io.github.freehij.authenticator.injections;

import com.google.common.net.InetAddresses;
import io.github.freehij.authenticator.data.Messages;
import io.github.freehij.authenticator.data.Values;
import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.authenticator.util.Sessions;
import io.github.freehij.authenticator.util.Utils;
import io.github.freehij.loader.annotation.AdvancedAt;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.annotation.Local;
import io.github.freehij.loader.constant.At;
import io.github.freehij.loader.constant.Shift;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@SuppressWarnings("deprecation")
@EditClass("net/minecraft/server/network/config/PrepareSpawnTask$Ready")
public class PrepareSpawnTaskInjection {
    @Inject(method = "spawn", at = At.NONE,
            advancedAt = @AdvancedAt(at = AdvancedAt.At.INVOKE,
                    optional = "net/minecraft/server/level/ServerPlayer;snapTo", shift = Shift.AFTER),
            locals = { @Local(index = 4, type = "Lnet/minecraft/server/level/ServerPlayer;" )}
    )
    public static void spawnInjection(InjectionHelper helper) {
        Connection connection = ((Connection) helper.getArg(1));
        if (Utils.isLocal(connection)) return;
        ServerPlayer player = (ServerPlayer) helper.getLocals()[0];
        if (Values.sessions) {
            String IP = getIpAddress(connection.getRemoteAddress());
            if (IP != null && Sessions.checkSession(player.getName().getString(), IP)) return;
        }
        PlayerAuthData.createNew(player);
    }

    @Inject(method = "spawn", at = At.RETURN,
            locals = { @Local(index = 4, type = "Lnet/minecraft/server/level/ServerPlayer;") })
    public static void spawnInjection2(InjectionHelper helper) {
        if (Utils.isLocal(((Connection) helper.getArg(1)))) return;
        ServerPlayer player = (ServerPlayer) helper.getLocals()[0];
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
