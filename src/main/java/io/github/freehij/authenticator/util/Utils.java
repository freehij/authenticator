package io.github.freehij.authenticator.util;

import io.github.freehij.loader.util.Reflector;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class Utils {
    public static boolean isLocal(Connection connection) {
        return connection.getLoggableAddress(true).startsWith("local");
    }

    public static boolean isLocal(ServerGamePacketListenerImpl connection) {
        Connection actualConnection = (Connection) new Reflector(
                ServerGamePacketListenerImpl.class, connection).getField("connection").get();
        return isLocal(actualConnection);
    }
}
