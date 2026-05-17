package io.github.freehij.authenticator.injections;

import io.github.freehij.authenticator.util.PlayerAuthData;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;

@SuppressWarnings("deprecation")
@EditClass("net/minecraft/world/item/BlockItem")
public class BlockItemInjection {
    @Inject(method = "canPlace")
    public static void canPlaceInjection(InjectionHelper helper) {
        Player player = ((BlockPlaceContext) helper.getArg(1)).getPlayer();
        if (player instanceof LocalPlayer) return;
        if (PlayerAuthData.exists(((ServerPlayer) player))) {
            helper.setReturnValue(false);
            helper.setCancelled(true);
        }
    }
}
