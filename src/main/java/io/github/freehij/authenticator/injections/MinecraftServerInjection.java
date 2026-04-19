package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.authenticator.player.PlayerAuthData;
import io.github.freehij.authenticator.value.Values;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.constant.At;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@SuppressWarnings("deprecation")
@EditClass("net/minecraft/server/MinecraftServer")
public class MinecraftServerInjection {
    @Inject(at = At.RETURN)
    public static void mainInjection(InjectionHelper helper) {
        Authenticator.init((MinecraftServer) helper.getSelf());
        new Thread(() -> {
            if (Values.saveInterval <= -1) return;
            while (Authenticator.server.isRunning()) {
                if (Authenticator.server.getTickCount() % Values.saveInterval == 0)
                    Authenticator.database.save();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Inject(method = "stopServer")
    public static void stopServerInjection(InjectionHelper helper) {
        PlayerList playerList = (PlayerList) helper.getReflector().getField("playerList").get();
        for (ServerPlayer player : playerList.getPlayers()) PlayerAuthData.removeSafe(player);
        Authenticator.database.save();
    }
}
