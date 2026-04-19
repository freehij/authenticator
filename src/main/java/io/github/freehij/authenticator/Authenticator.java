package io.github.freehij.authenticator;

import io.github.freehij.authenticator.player.PlayerAuthData;
import io.github.freehij.authenticator.util.Config;
import io.github.freehij.authenticator.util.DataBase;
import io.github.freehij.authenticator.value.Messages;
import io.github.freehij.authenticator.value.Values;
import io.github.freehij.loader.util.Logger;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class Authenticator {
    public static final String MOD_ID = "Authenticator";
    static final Config config = new Config("config/authenticator.json");
    public static final DataBase database = new DataBase("database/authenticator");
    public static MinecraftServer server;

    public static void init(MinecraftServer server) {
        Logger.info("Initializing Authenticator.", MOD_ID);
        Authenticator.server = server;
        Messages.register = config.get("register_message",
                "§cPlease register using /reg <password>.");
        Messages.login = config.get("login_message", "§cPlease login using /l <password>.");
        Messages.alreadyRegistered = config.get("already_registered", "§cThis account is already registered.");
        Messages.wrongPass = config.get("wrong_password", "§cWrong password!");
        Messages.notLoggedIn = config.get("not_logged_in", "§cYou must login first!");
        Messages.authSuccess = config.get("auth_success", "§aYou've been successfully authenticated!");
        Messages.notRegistered = config.get("not_registered", "§cYou must register first!");
        Messages.tooManyAttempts = config.get("too_many_attempts", "§cToo many attempts!");
        Messages.tookTooLongToLogin = config.get("took_too_long_to_login", "§cTook too long to login!");
        Messages.passwordTooLong = config.get("password_too_long", "§cPassword is too long. Max is %s symbols.");
        Messages.passwordTooSmall = config.get("password_too_small", "§cPassword is too small. Min is %s symbols.");
        Values.minPasswordLength = config.get("min_password_length", 3);
        Values.maxPasswordLength = config.get("max_password_length", 16);
        Values.maxLoginAttempts = config.get("max_login_attempts", 5);
        Values.loginTimeOut = config.get("login_timeout", 1200);
        Values.saveInterval = config.get("save_interval", -1);
        config.save();
    }

    public static void onPlayerAdd(ServerPlayer player) {
        PlayerAuthData.createNew(player);
        if (player.isDeadOrDying()) player.setHealth(20);
        player.setPos(0, 65, 0);
        player.setYRot(0);
        player.setXRot(0);
    }
}
