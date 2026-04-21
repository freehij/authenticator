package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;

@SuppressWarnings("deprecation")
@EditClass("net/minecraft/world/item/BlockItem")
public class BlockItemInjection {
    @Inject(method = "canPlace")
    public static void canPlaceInjection(InjectionHelper helper) {
        if (PlayerAuthData.exists(((ServerPlayer) ((BlockPlaceContext) helper.getArg(1)).getPlayer()))) {
            helper.setReturnValue(false);
            helper.setCancelled(true);
        }
    }
}
