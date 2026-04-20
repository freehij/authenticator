package io.github.freehij.authenticator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.authenticator.player.PlayerAuthData;
import io.github.freehij.authenticator.util.Sessions;
import io.github.freehij.authenticator.value.Messages;
import io.github.freehij.authenticator.value.Values;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AuthenticatorCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> registerNode = dispatcher.register(Commands.literal("register")
                .then(Commands.argument("password", StringArgumentType.greedyString())
                        .executes((c) -> {
                            if (c.getSource().isPlayer()) {
                                ServerPlayer player = c.getSource().getPlayer();
                                if (!PlayerAuthData.exists(player)) return 1;
                                String username = c.getSource().getTextName();
                                if (!Authenticator.database.isRegistered(username)) {
                                    String password = StringArgumentType.getString(c, "password");
                                    if (password.length() > Values.maxPasswordLength) {
                                        c.getSource().sendSystemMessage(Component.literal(
                                                Messages.replaceSpecialSequence(Messages.passwordTooLong,
                                                        Values.maxPasswordLength)));
                                        return 1;
                                    }
                                    if (password.length() < Values.minPasswordLength) {
                                        c.getSource().sendSystemMessage(Component.literal(
                                                Messages.replaceSpecialSequence(Messages.passwordTooSmall,
                                                        Values.minPasswordLength)));
                                        return 1;
                                    }
                                    Authenticator.database.set(username, StringArgumentType.getString(c, "password"));
                                    Sessions.updateSession(username, player.getIpAddress());
                                    snapPlayer(c.getSource());
                                } else {
                                    c.getSource().sendSystemMessage(Component.literal(Messages.alreadyRegistered));
                                }
                            } else {
                                c.getSource().sendSystemMessage(
                                        Component.literal("Only players can execute this command."));
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
                                    PlayerAuthData.createNew(c.getSource().getPlayer());
                                    c.getSource().sendSystemMessage(Component.literal(Messages.unRegSuccess));
                                } else {
                                    c.getSource().sendSystemMessage(Component.literal(Messages.wrongPass));
                                }
                            } else {
                                c.getSource().sendSystemMessage(
                                        Component.literal("Only players can execute this command."));
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
                                        Sessions.updateSession(username, player.getIpAddress());
                                        snapPlayer(c.getSource());
                                    } else {
                                        if (PlayerAuthData.increaseAttempts(player)) {
                                            player.connection.disconnect(Component.literal(Messages.tooManyAttempts));
                                        } else {
                                            c.getSource().sendSystemMessage(Component.literal(Messages.wrongPass));
                                        }
                                    }
                                } else {
                                    c.getSource().sendSystemMessage(Component.literal(Messages.notRegistered));
                                }
                            } else {
                                c.getSource().sendSystemMessage(
                                        Component.literal("Only players can execute this command."));
                            }
                            return 1;
                        })));
        dispatcher.register(Commands.literal("l").redirect(loginNode));
        dispatcher.register(Commands.literal("authenticator")
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.literal("login")
                        .then(Commands.argument("target", StringArgumentType.string())
                                .suggests(PLAYERS_SUGGESTION)
                                .executes((c) -> {
                                    String target = StringArgumentType.getString(c, "target");
                                    ServerPlayer player = Authenticator.server.getPlayerList().getPlayerByName(target);
                                    if (player == null) {
                                        c.getSource().sendSystemMessage(Component.literal("§cPlayer is offline."));
                                        return 1;
                                    }
                                    if (!PlayerAuthData.exists(player)) {
                                        c.getSource().sendSystemMessage(
                                                Component.literal("§cPlayer is already logged in."));
                                        return 1;
                                    }
                                    PlayerAuthData.removeSafe(player);
                                    c.getSource().sendSystemMessage(
                                            Component.literal("§aSuccessfully marked player as logged in."));
                                    player.sendSystemMessage(
                                            Component.literal("You've been authenticated via an admin command."));
                                    return 1;
                                })))
                .then(Commands.literal("register")
                        .then(Commands.argument("target", StringArgumentType.string())
                                .suggests(PLAYERS_SUGGESTION)
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
                        .then(Commands.argument("target", StringArgumentType.string())
                                .suggests(PLAYERS_SUGGESTION).executes((c) -> {
                                    executeAdminUnregister(c, false);
                                    return 1;
                                }).then(Commands.argument("logout1", BoolArgumentType.bool()).executes((c) -> {
                                    executeAdminUnregister(c, BoolArgumentType.getBool(c, "logout1"));
                                    return 1;
                                }))))
                .then(Commands.literal("logout")
                        .then(Commands.argument("target", StringArgumentType.string())
                                .suggests(PLAYERS_SUGGESTION)
                                .executes((c) -> {
                                    String target = StringArgumentType.getString(c, "target");
                                    ServerPlayer player = Authenticator.server.getPlayerList().getPlayerByName(target);
                                    if (player == null) {
                                        c.getSource().sendSystemMessage(Component.literal("§cPlayer is offline."));
                                        return 1;
                                    }
                                    if (PlayerAuthData.exists(player)) {
                                        c.getSource().sendSystemMessage(
                                                Component.literal("§cPlayer is not logged in."));
                                        return 1;
                                    }
                                    PlayerAuthData.createNew(player);
                                    Sessions.eraseSession(target);
                                    c.getSource().sendSystemMessage(
                                            Component.literal("§aSuccessfully marked player as not logged in."));
                                    player.sendSystemMessage(
                                            Component.literal("You've been deauthenticated via an admin command."));
                                    return 1;
                                }))));
    }

    static final SuggestionProvider<CommandSourceStack> PLAYERS_SUGGESTION = (c, b) -> {
        for (ServerPlayer player : c.getSource().getServer().getPlayerList().getPlayers()) {
            b.suggest(player.getName().getString());
        }
        return b.buildFuture();
    };

    static void executeAdminUnregister(CommandContext<CommandSourceStack> context, boolean shouldLogout) {
        String username = StringArgumentType.getString(context, "target");
        if (!Authenticator.database.isRegistered(username)) {
            context.getSource().sendSystemMessage(Component.literal("§cPlayer is not registered."));
            return;
        }
        Authenticator.database.remove(username);
        ServerPlayer player = Authenticator.server.getPlayerList().getPlayerByName(username);
        if (shouldLogout) {
            if (player != null) {
                if (!PlayerAuthData.exists(player)) PlayerAuthData.createNew(player);
                player.sendSystemMessage(Component.literal("You've been deauthenticated via an admin command."));
                context.getSource().sendSystemMessage(
                        Component.literal("§aSuccessfully marked player as unregistered and not logged in."));
            } else {
                context.getSource().sendSystemMessage(
                        Component.literal("§aSuccessfully marked player as unregistered."));
                context.getSource().sendSystemMessage(Component.literal("§cCouldn't logout, the player is offline."));
            }
            return;
        }
        context.getSource().sendSystemMessage(Component.literal("§aSuccessfully marked player as unregistered."));
    }

    static void executeAdminRegister(CommandContext<CommandSourceStack> context, boolean shouldLogin) {
        String username = StringArgumentType.getString(context, "target");
        if (Authenticator.database.isRegistered(username)) {
            context.getSource().sendSystemMessage(Component.literal("§cPlayer is already registered."));
            return;
        }
        String password = StringArgumentType.getString(context, "password");
        if (password.length() > Values.maxPasswordLength) {
            context.getSource().sendSystemMessage(Component.literal(Messages.replaceSpecialSequence(
                    Messages.passwordTooLong, Values.maxPasswordLength)));
            return;
        }
        if (password.length() < Values.minPasswordLength) {
            context.getSource().sendSystemMessage(Component.literal(Messages.replaceSpecialSequence(
                    Messages.passwordTooSmall, Values.minPasswordLength)));
            return;
        }
        Authenticator.database.set(username, password);
        if (shouldLogin) {
            ServerPlayer player = Authenticator.server.getPlayerList().getPlayerByName(username);
            if (player != null) {
                PlayerAuthData.removeSafe(player);
                player.sendSystemMessage(Component.literal("You've been authenticated via an admin command."));
                context.getSource().sendSystemMessage(
                        Component.literal("§aSuccessfully marked player as registered and logged in."));
            } else {
                context.getSource().sendSystemMessage(
                        Component.literal("§aSuccessfully marked player as registered."));
                context.getSource().sendSystemMessage(Component.literal("§cCouldn't login, the player is offline."));
            }
            return;
        }
        context.getSource().sendSystemMessage(Component.literal("§aSuccessfully marked player as registered."));
    }

    static void snapPlayer(CommandSourceStack c) {
        c.sendSystemMessage(Component.literal(Messages.authSuccess));
        PlayerAuthData.removeSafe(c.getPlayer());
    }
}