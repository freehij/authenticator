package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("deprecation")
@EditClass("net/minecraft/server/players/PlayerList")
public class PlayerListInjection {
    @Inject(method = "placeNewPlayer")
    public static void placeNewPlayerInjection(InjectionHelper helper) {
        Authenticator.onPlayerAdd((ServerPlayer) helper.getArg(2));
    }
}
