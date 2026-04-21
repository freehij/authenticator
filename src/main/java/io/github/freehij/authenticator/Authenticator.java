package io.github.freehij.authenticator;

import io.github.freehij.authenticator.util.Config;
import io.github.freehij.authenticator.util.DataBase;
import io.github.freehij.authenticator.data.Messages;
import io.github.freehij.authenticator.data.Values;
import io.github.freehij.loader.util.Logger;
import net.minecraft.server.MinecraftServer;

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
        Messages.sessionLogin = config.get("session_login", "You've been authenticated via your previous session.");
        Messages.unRegSuccess = config.get("unreg_success", "§aYour account have been successfully removed.");
        Values.minPasswordLength = config.get("min_password_length", 3);
        Values.maxPasswordLength = config.get("max_password_length", 16);
        Values.maxLoginAttempts = config.get("max_login_attempts", 5);
        Values.loginTimeOut = config.get("login_timeout", 1200);
        Values.saveInterval = config.get("save_interval", -1);
        Values.sessions = config.get("sessions", false);
        Values.sessionTime = config.get("session_time", 1800);
        Values.compressDatabase = config.get("compress_database", Values.compressDatabase);
        Values.encryptionType = config.get("encryption_type", Values.encryptionType);
        config.save();
    }
}
