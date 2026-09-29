package io.github.freehij.authenticator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.authenticator.data.*;
import io.github.freehij.authenticator.util.*;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class AuthenticatorCommands {
    static final List<String> MESSAGE_KEYS = Arrays.asList(
            "register_message", "login_message", "already_registered", "wrong_pass",
            "not_logged_in", "auth_success", "not_registered", "too_many_attempts",
            "took_too_long_to_login", "password_too_long", "password_too_small",
            "session_login", "unreg_success"
    );

    static final List<String> VALUE_KEYS = Arrays.asList(
            "min_password_length", "max_password_length", "max_login_attempts",
            "login_timeout", "save_interval", "sessions", "session_time",
            "compress_database", "encryption_type"
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> registerNode = dispatcher.register(
                Commands.literal("register")
                        .then(Commands.argument("password", StringArgumentType.greedyString())
                                .executes(context -> {
                                    executeRegister(context);
                                    return 1;
                                }))
        );
        dispatcher.register(Commands.literal("reg").redirect(registerNode));

        LiteralCommandNode<CommandSourceStack> unregisterNode = dispatcher.register(
                Commands.literal("unregister")
                        .then(Commands.argument("password", StringArgumentType.greedyString())
                                .executes(context -> {
                                    executeUnregister(context);
                                    return 1;
                                }))
        );
        dispatcher.register(Commands.literal("unreg").redirect(unregisterNode));

        LiteralCommandNode<CommandSourceStack> loginNode = dispatcher.register(
                Commands.literal("login")
                        .then(Commands.argument("password", StringArgumentType.greedyString())
                                .executes(context -> {
                                    executeLogin(context);
                                    return 1;
                                }))
        );
        dispatcher.register(Commands.literal("l").redirect(loginNode));

        dispatcher.register(
                Commands.literal("authenticator")
                        .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .then(Commands.literal("login")
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .suggests(PLAYERS_SUGGESTION)
                                        .executes(context -> {
                                            executeAdminLogin(context);
                                            return 1;
                                        })))
                        .then(Commands.literal("register")
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .suggests(PLAYERS_SUGGESTION)
                                        .then(Commands.argument("password", StringArgumentType.string())
                                                .executes(context -> {
                                                    executeAdminRegister(context, false);
                                                    return 1;
                                                })
                                                .then(Commands.argument("login", BoolArgumentType.bool())
                                                        .executes(context -> {
                                                            executeAdminRegister(
                                                                    context,
                                                                    BoolArgumentType.getBool(context, "login")
                                                            );
                                                            return 1;
                                                        })))))
                        .then(Commands.literal("unregister")
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .suggests(PLAYERS_SUGGESTION)
                                        .executes(context -> {
                                            executeAdminUnregister(context, false);
                                            return 1;
                                        })
                                        .then(Commands.argument("logout", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    executeAdminUnregister(
                                                            context,
                                                            BoolArgumentType.getBool(context, "logout")
                                                    );
                                                    return 1;
                                                }))))
                        .then(Commands.literal("logout")
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .suggests(PLAYERS_SUGGESTION)
                                        .executes(context -> {
                                            executeAdminLogout(context);
                                            return 1;
                                        })))
                        .then(Commands.literal("messages")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("key", StringArgumentType.string())
                                                .suggests(MESSAGE_KEYS_SUGGESTION)
                                                .executes(context -> {
                                                    executeMessageGet(context);
                                                    return 1;
                                                })))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("key", StringArgumentType.string())
                                                .suggests(MESSAGE_KEYS_SUGGESTION)
                                                .then(Commands.argument("value", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            executeMessageSet(context);
                                                            return 1;
                                                        })))))
                        .then(Commands.literal("values")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("key", StringArgumentType.string())
                                                .suggests(VALUE_KEYS_SUGGESTION)
                                                .executes(context -> {
                                                    executeValueGet(context);
                                                    return 1;
                                                })))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("key", StringArgumentType.string())
                                                .suggests(VALUE_KEYS_SUGGESTION)
                                                .then(Commands.argument("value", StringArgumentType.greedyString())
                                                        .executes(context -> {
                                                            executeValueSet(context);
                                                            return 1;
                                                        })))))
        );
    }

    static final SuggestionProvider<CommandSourceStack> PLAYERS_SUGGESTION = (context, builder) -> {
        for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
            if (Utils.isLocal(player.connection)) continue;
            builder.suggest(player.getName().getString());
        }
        return builder.buildFuture();
    };

    static final SuggestionProvider<CommandSourceStack> MESSAGE_KEYS_SUGGESTION =
            (context, builder) -> {
                MESSAGE_KEYS.forEach(builder::suggest);
                return builder.buildFuture();
            };

    static final SuggestionProvider<CommandSourceStack> VALUE_KEYS_SUGGESTION =
            (context, builder) -> {
                VALUE_KEYS.forEach(builder::suggest);
                return builder.buildFuture();
            };

    static void executeRegister(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            source.sendSystemMessage(Component.literal("Only players can execute this command."));
            return;
        }
        ServerPlayer player = source.getPlayer();
        if (Utils.isLocal(player.connection)) return;
        if (!PlayerAuthData.exists(player)) return;

        String username = source.getTextName();
        if (Authenticator.database.isRegistered(username)) {
            source.sendSystemMessage(Component.literal(Messages.alreadyRegistered));
            return;
        }

        String password = StringArgumentType.getString(context, "password");
        if (password.length() > Values.maxPasswordLength) {
            source.sendSystemMessage(Component.literal(
                    Messages.replaceSpecialSequence(Messages.passwordTooLong, Values.maxPasswordLength)));
            return;
        }
        if (password.length() < Values.minPasswordLength) {
            source.sendSystemMessage(Component.literal(
                    Messages.replaceSpecialSequence(Messages.passwordTooSmall, Values.minPasswordLength)));
            return;
        }

        Authenticator.database.set(username, password);
        Sessions.updateSession(username, player.getIpAddress());
        snapPlayer(source);
    }

    static void executeUnregister(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            source.sendSystemMessage(Component.literal("Only players can execute this command."));
            return;
        }
        ServerPlayer player = source.getPlayer();
        if (Utils.isLocal(player.connection)) return;

        String username = source.getTextName();
        String password = StringArgumentType.getString(context, "password");
        if (Authenticator.database.checkPassword(username, password)) {
            Authenticator.database.remove(username);
            PlayerAuthData.createNew(player);
            source.sendSystemMessage(Component.literal(Messages.unRegSuccess));
        } else {
            source.sendSystemMessage(Component.literal(Messages.wrongPass));
        }
    }

    static void executeLogin(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            source.sendSystemMessage(Component.literal("Only players can execute this command."));
            return;
        }
        ServerPlayer player = source.getPlayer();
        if (Utils.isLocal(player.connection)) return;
        if (!PlayerAuthData.exists(player)) return;

        String username = source.getTextName();
        if (!Authenticator.database.isRegistered(username)) {
            source.sendSystemMessage(Component.literal(Messages.notRegistered));
            return;
        }

        String password = StringArgumentType.getString(context, "password");
        if (Authenticator.database.checkPassword(username, password)) {
            Sessions.updateSession(username, player.getIpAddress());
            snapPlayer(source);
        } else {
            if (PlayerAuthData.increaseAttempts(player)) {
                player.connection.disconnect(Component.literal(Messages.tooManyAttempts));
            } else {
                source.sendSystemMessage(Component.literal(Messages.wrongPass));
            }
        }
    }

    static void executeAdminLogin(CommandContext<CommandSourceStack> context) {
        String target = StringArgumentType.getString(context, "target");
        ServerPlayer player = Authenticator.server.getPlayerList().getPlayerByName(target);
        if (player == null) {
            context.getSource().sendSystemMessage(Component.literal("§cPlayer is offline."));
            return;
        }
        if (Utils.isLocal(player.connection)) return;
        if (!PlayerAuthData.exists(player)) {
            context.getSource().sendSystemMessage(Component.literal("§cPlayer is already logged in."));
            return;
        }

        PlayerAuthData.removeSafe(player);
        context.getSource().sendSystemMessage(Component.literal("§aSuccessfully marked player as logged in."));
        player.sendSystemMessage(Component.literal("You've been authenticated via an admin command."));
    }

    static void executeAdminLogout(CommandContext<CommandSourceStack> context) {
        String target = StringArgumentType.getString(context, "target");
        ServerPlayer player = Authenticator.server.getPlayerList().getPlayerByName(target);
        if (player == null) {
            context.getSource().sendSystemMessage(Component.literal("§cPlayer is offline."));
            return;
        }
        if (Utils.isLocal(player.connection)) return;
        if (PlayerAuthData.exists(player)) {
            context.getSource().sendSystemMessage(Component.literal("§cPlayer is not logged in."));
            return;
        }

        PlayerAuthData.createNew(player);
        Sessions.eraseSession(target);
        context.getSource().sendSystemMessage(Component.literal("§aSuccessfully marked player as not logged in."));
        player.sendSystemMessage(Component.literal("You've been deauthenticated via an admin command."));
    }

    static void executeAdminRegister(CommandContext<CommandSourceStack> context, boolean shouldLogin) {
        String username = StringArgumentType.getString(context, "target");
        ServerPlayer player = Authenticator.server.getPlayerList().getPlayerByName(username);
        if (player != null && Utils.isLocal(player.connection)) return;

        if (Authenticator.database.isRegistered(username)) {
            context.getSource().sendSystemMessage(Component.literal("§cPlayer is already registered."));
            return;
        }

        String password = StringArgumentType.getString(context, "password");
        if (password.length() > Values.maxPasswordLength) {
            context.getSource().sendSystemMessage(Component.literal(
                    Messages.replaceSpecialSequence(Messages.passwordTooLong, Values.maxPasswordLength)));
            return;
        }
        if (password.length() < Values.minPasswordLength) {
            context.getSource().sendSystemMessage(Component.literal(
                    Messages.replaceSpecialSequence(Messages.passwordTooSmall, Values.minPasswordLength)));
            return;
        }

        Authenticator.database.set(username, password);

        if (shouldLogin) {
            if (player != null) {
                PlayerAuthData.removeSafe(player);
                player.sendSystemMessage(Component.literal("You've been authenticated via an admin command."));
                context.getSource().sendSystemMessage(
                        Component.literal("§aSuccessfully marked player as registered and logged in."));
            } else {
                context.getSource().sendSystemMessage(Component.literal("§aSuccessfully marked player as registered."));
                context.getSource().sendSystemMessage(Component.literal("§cCouldn't login, the player is offline."));
            }
        } else {
            context.getSource().sendSystemMessage(Component.literal("§aSuccessfully marked player as registered."));
        }
    }

    static void executeAdminUnregister(CommandContext<CommandSourceStack> context, boolean shouldLogout) {
        String username = StringArgumentType.getString(context, "target");
        ServerPlayer player = Authenticator.server.getPlayerList().getPlayerByName(username);
        if (player != null && Utils.isLocal(player.connection)) return;

        if (!Authenticator.database.isRegistered(username)) {
            context.getSource().sendSystemMessage(Component.literal("§cPlayer is not registered."));
            return;
        }

        Authenticator.database.remove(username);

        if (shouldLogout) {
            if (player != null) {
                if (!PlayerAuthData.exists(player)) {
                    PlayerAuthData.createNew(player);
                }
                player.sendSystemMessage(Component.literal("You've been deauthenticated via an admin command."));
                context.getSource().sendSystemMessage(
                        Component.literal("§aSuccessfully marked player as unregistered and not logged in."));
            } else {
                context.getSource().sendSystemMessage(Component.literal("§aSuccessfully marked player as unregistered."));
                context.getSource().sendSystemMessage(Component.literal("§cCouldn't logout, the player is offline."));
            }
        } else {
            context.getSource().sendSystemMessage(Component.literal("§aSuccessfully marked player as unregistered."));
        }
    }

    static void executeMessageGet(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "key");
        String value = Utils.getMessage(key);
        if (value == null) {
            context.getSource().sendSystemMessage(Component.literal("§cUnknown message key."));
        } else {
            context.getSource().sendSystemMessage(Component.literal("§a" + key + " = " + value));
        }
    }

    static void executeMessageSet(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "key");
        String value = StringArgumentType.getString(context, "value");
        if (Utils.setMessage(key, value)) {
            context.getSource().sendSystemMessage(Component.literal("§aMessage updated."));
        } else {
            context.getSource().sendSystemMessage(Component.literal("§cUnknown message key."));
        }
    }

    static void executeValueGet(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "key");
        String value = Utils.getValue(key);
        if (value == null) {
            context.getSource().sendSystemMessage(Component.literal("§cUnknown value key."));
        } else {
            context.getSource().sendSystemMessage(Component.literal("§a" + key + " = " + value));
        }
    }

    static void executeValueSet(CommandContext<CommandSourceStack> context) {
        String key = StringArgumentType.getString(context, "key");
        String value = StringArgumentType.getString(context, "value");
        if (Utils.setValue(key, value)) {
            context.getSource().sendSystemMessage(Component.literal("§aValue updated."));
        } else {
            context.getSource().sendSystemMessage(Component.literal("§cInvalid value or key."));
        }
    }

    static void snapPlayer(CommandSourceStack source) {
        source.sendSystemMessage(Component.literal(Messages.authSuccess));
        PlayerAuthData.removeSafe(source.getPlayer());
    }
}
