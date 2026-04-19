package io.github.freehij.authenticator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.authenticator.player.PlayerAuthData;
import io.github.freehij.authenticator.value.Messages;
import io.github.freehij.authenticator.value.Values;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AuthenticatorCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> registerNode = dispatcher.register(Commands.literal("register")
                .then(Commands.argument("password", StringArgumentType.greedyString())
                .executes((c) -> {
                    if (c.getSource().isPlayer()) {
                        if (!PlayerAuthData.exists(c.getSource().getPlayer())) return 1;
                        String username = c.getSource().getTextName();
                        if (!Authenticator.database.isRegistered(username)) {
                            String password = StringArgumentType.getString(c, "password");
                            if (password.length() > Values.maxPasswordLength) {
                                c.getSource().sendSuccess(() ->
                                        Component.literal(Messages.replaceSpecialSequence(
                                                Messages.passwordTooLong, Values.maxPasswordLength)), false);
                                return 1;
                            }
                            if (password.length() < Values.minPasswordLength) {
                                c.getSource().sendSuccess(() ->
                                        Component.literal(Messages.replaceSpecialSequence(
                                                Messages.passwordTooSmall, Values.minPasswordLength)), false);
                                return 1;
                            }
                            Authenticator.database.set(username, StringArgumentType.getString(c, "password"));
                            snapPlayer(c.getSource());
                        } else {
                            c.getSource().sendSuccess(() -> Component.literal(Messages.alreadyRegistered), false);
                        }
                    } else {
                        c.getSource().sendSystemMessage(Component.literal("Only players can execute this command."));
                    }
                    return 1;
        })));
        dispatcher.register(Commands.literal("reg").redirect(registerNode));
        LiteralCommandNode<CommandSourceStack> unregisterNode = dispatcher.register(Commands.literal("unregister")
                .then(Commands.argument("password", StringArgumentType.greedyString())
                .executes((c) -> {
                    if (c.getSource().isPlayer()) {
                        String username = c.getSource().getTextName();
                        if (Authenticator.database.checkPassword(username,
                                StringArgumentType.getString(c, "password"))) {
                            Authenticator.database.remove(username);
                            Authenticator.onPlayerAdd(c.getSource().getPlayer());
                        } else {
                            c.getSource().sendSuccess(() -> Component.literal(Messages.wrongPass), false);
                        }
                    } else {
                        c.getSource().sendSystemMessage(Component.literal("Only players can execute this command."));
                    }
                    return 1;
        })));
        dispatcher.register(Commands.literal("unreg").redirect(unregisterNode));
        LiteralCommandNode<CommandSourceStack> loginNode = dispatcher.register(Commands.literal("login")
                .then(Commands.argument("password", StringArgumentType.greedyString())
                .executes((c) -> {
                    if (c.getSource().isPlayer()) {
                        ServerPlayer player = c.getSource().getPlayer();
                        if (!PlayerAuthData.exists(player)) return 1;
                        String username = c.getSource().getTextName();
                        if (Authenticator.database.isRegistered(username)) {
                            if (Authenticator.database.checkPassword(username,
                                    StringArgumentType.getString(c, "password"))) {
                                snapPlayer(c.getSource());
                            } else {
                                if (PlayerAuthData.increaseAttempts(player)) {
                                    player.connection.disconnect(Component.literal(Messages.tooManyAttempts));
                                } else {
                                    c.getSource().sendSuccess(() -> Component.literal(Messages.wrongPass), false);
                                }
                            }
                        } else {
                            c.getSource().sendSuccess(() -> Component.literal(Messages.notRegistered), false);
                        }
                    } else {
                        c.getSource().sendSystemMessage(Component.literal("Only players can execute this command."));
                    }
                    return 1;
        })));
        dispatcher.register(Commands.literal("l").redirect(loginNode));
        dispatcher.register(Commands.literal("authenticator")
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.literal("login")
                        .then(Commands.argument("target", EntityArgument.player()).executes((c) -> {
                            try {
                                ServerPlayer player = EntityArgument.getPlayer(c, "target");
                                if (!PlayerAuthData.exists(player)) {
                                    c.getSource().sendSuccess(() ->
                                                    Component.literal("§cPlayer is already logged in."), false);
                                    return 1;
                                }
                                PlayerAuthData.removeSafe(player);
                                c.getSource().sendSuccess(() ->
                                        Component.literal("§aSuccessfully marked player as logged in."), false);
                                player.sendSystemMessage(
                                        Component.literal("You've been authenticated via an admin command."));
                            } catch (Exception e) {
                                c.getSource().sendSuccess(() -> Component.literal("§cPlayer is offline."), false);
                            }
                            return 1;
                        })))
                .then(Commands.literal("register")
                        .then(Commands.argument("target", StringArgumentType.string())
                                .then(Commands.argument("password", StringArgumentType.string())
                                        .executes((c) -> {
                                            executeAdminRegister(c, false);
                                            return 1;
                                        }).then(Commands.argument("login", BoolArgumentType.bool())
                                                .executes((c) -> {
                                                    executeAdminRegister(c, BoolArgumentType.getBool(c, "login"));
                                                    return 1;
                                                })))))
                .then(Commands.literal("unregister")
                        .then(Commands.argument("target", StringArgumentType.string()).executes((c) -> {
                            executeAdminUnregister(c, false);
                            return 1;
                        }).then(Commands.argument("logout1", BoolArgumentType.bool()).executes((c) -> {
                            executeAdminUnregister(c, BoolArgumentType.getBool(c, "logout1"));
                            return 1;
                        }))))
                .then(Commands.literal("logout")
                        .then(Commands.argument("target", EntityArgument.player()).executes((c) -> {
                            try {
                                ServerPlayer player = EntityArgument.getPlayer(c, "target");
                                if (PlayerAuthData.exists(player)) {
                                    c.getSource().sendSuccess(() ->
                                            Component.literal("§cPlayer is not logged in."), false);
                                    return 1;
                                }
                                PlayerAuthData.createNew(player);
                                c.getSource().sendSuccess(() ->
                                        Component.literal("§aSuccessfully marked player as not logged in."), false);
                                player.sendSystemMessage(
                                        Component.literal("You've been deauthenticated via an admin command."));
                            } catch (Exception e) {
                                c.getSource().sendSuccess(() -> Component.literal("§cPlayer is offline."), false);
                            }
                            return 1;
                        }))));
    }

    static void executeAdminUnregister(CommandContext<CommandSourceStack> context, boolean shouldLogout) {
        String username = StringArgumentType.getString(context, "target");
        if (!Authenticator.database.isRegistered(username)) {
            context.getSource().sendSuccess(() -> Component.literal("§cPlayer is not registered."), false);
            return;
        }
        Authenticator.database.remove(username);
        ServerPlayer player = Authenticator.server.getPlayerList().getPlayer(username);
        if (shouldLogout) {
            if (player != null) {
                if (!PlayerAuthData.exists(player)) PlayerAuthData.createNew(player);
                player.sendSystemMessage(Component.literal("You've been deauthenticated via an admin command."));
                context.getSource().sendSuccess(() ->
                        Component.literal("§aSuccessfully marked player as unregistered and not logged in."), false);
            } else {
                context.getSource().sendSuccess(() ->
                        Component.literal("§aSuccessfully marked player as unregistered."), false);
                context.getSource().sendSuccess(() -> Component.literal("§cCouldn't logout, the player is offline."),
                        false);
            }
            return;
        }
        context.getSource().sendSuccess(() ->
                Component.literal("§aSuccessfully marked player as unregistered."), false);
    }

    static void executeAdminRegister(CommandContext<CommandSourceStack> context, boolean shouldLogin) {
        String username = StringArgumentType.getString(context, "target");
        if (Authenticator.database.isRegistered(username)) {
            context.getSource().sendSuccess(() -> Component.literal("§cPlayer is already registered."), false);
            return;
        }
        String password = StringArgumentType.getString(context, "password");
        if (password.length() > Values.maxPasswordLength) {
            context.getSource().sendSuccess(() -> Component.literal(Messages.replaceSpecialSequence(
                            Messages.passwordTooLong, Values.maxPasswordLength)), false);
            return;
        }
        if (password.length() < Values.minPasswordLength) {
            context.getSource().sendSuccess(() -> Component.literal(Messages.replaceSpecialSequence(
                    Messages.passwordTooSmall, Values.minPasswordLength)), false);
            return;
        }
        Authenticator.database.set(username, password);
        if (shouldLogin) {
            ServerPlayer player = Authenticator.server.getPlayerList().getPlayer(username);
            if (player != null) {
                PlayerAuthData.removeSafe(player);
                player.sendSystemMessage(Component.literal("You've been authenticated via an admin command."));
                context.getSource().sendSuccess(() ->
                        Component.literal("§aSuccessfully marked player as registered and logged in."), false);
            } else {
                context.getSource().sendSuccess(() ->
                        Component.literal("§aSuccessfully marked player as registered."), false);
                context.getSource().sendSuccess(() -> Component.literal("§cCouldn't login, the player is offline."),
                        false);
            }
            return;
        }
        context.getSource().sendSuccess(() ->
                Component.literal("§aSuccessfully marked player as registered."), false);
    }

    static void snapPlayer(CommandSourceStack c) {
        c.sendSuccess(() -> Component.literal(Messages.authSuccess), false);
        PlayerAuthData.removeSafe(c.getPlayer());
    }
}
