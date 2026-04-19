package io.github.freehij.authenticator.injections;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import io.github.freehij.authenticator.command.AuthenticatorCommands;
import io.github.freehij.authenticator.player.PlayerAuthData;
import io.github.freehij.authenticator.value.Messages;
import io.github.freehij.loader.annotation.EditClass;
import io.github.freehij.loader.annotation.Inject;
import io.github.freehij.loader.constant.At;
import io.github.freehij.loader.util.InjectionHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings({"unchecked", "deprecation", "rawtypes"})
@EditClass("net/minecraft/commands/Commands")
public class CommandInitializer {
    @Inject(at = At.RETURN)
    public static void initInjection(InjectionHelper helper) {
        CommandDispatcher<CommandSourceStack> dispatcher =
                (CommandDispatcher<CommandSourceStack>) helper.getReflector().getField("dispatcher").get();
        AuthenticatorCommands.register(dispatcher);
    }

    @Inject(method = "performCommand")
    public static void performCommandInjection(InjectionHelper helper) {
        ParseResults command = (ParseResults) helper.getArg(1);
        CommandSourceStack source = ((CommandSourceStack) command.getContext().getSource());
        if (source.isPlayer()) {
            ServerPlayer player = source.getPlayer();
            if (PlayerAuthData.exists(player) && !isAllowed(command.getReader().getString())) {
                source.sendFailure(Component.literal(Messages.notLoggedIn));
                helper.setCancelled(true);
            }
        }
    }

    static boolean isAllowed(String commandLine) {
        return commandLine.startsWith("reg ") || commandLine.startsWith("l ") || commandLine.startsWith("login ") ||
                commandLine.startsWith("register ");
    }
}
