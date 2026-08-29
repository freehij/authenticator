package io.github.freehij.authenticator.util;

import io.github.freehij.authenticator.Authenticator;
import io.github.freehij.authenticator.data.*;
import io.github.freehij.loader.util.Reflector;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class Utils {
    public static boolean isLocal(Connection connection) {
        return connection.getLoggableAddress(true).startsWith("local");
    }

    public static boolean isLocal(ServerGamePacketListenerImpl connection) {
        Connection actualConnection = (Connection) new Reflector(
                ServerGamePacketListenerImpl.class, connection).getField("connection").get();
        return isLocal(actualConnection);
    }

    public static String getMessage(String key) {
        return switch (key) {
            case "register_message" -> Messages.register;
            case "login_message" -> Messages.login;
            case "already_registered" -> Messages.alreadyRegistered;
            case "wrong_password" -> Messages.wrongPass;
            case "not_logged_in" -> Messages.notLoggedIn;
            case "auth_success" -> Messages.authSuccess;
            case "not_registered" -> Messages.notRegistered;
            case "too_many_attempts" -> Messages.tooManyAttempts;
            case "took_too_long_to_login" -> Messages.tookTooLongToLogin;
            case "password_too_long" -> Messages.passwordTooLong;
            case "password_too_small" -> Messages.passwordTooSmall;
            case "session_login" -> Messages.sessionLogin;
            case "unreg_success" -> Messages.unRegSuccess;
            default -> null;
        };
    }

    public static boolean setMessage(String key, String value) {
        switch (key) {
            case "register_message": Messages.register = value; break;
            case "login_message": Messages.login = value; break;
            case "already_registered": Messages.alreadyRegistered = value; break;
            case "wrong_password": Messages.wrongPass = value; break;
            case "not_logged_in": Messages.notLoggedIn = value; break;
            case "auth_success": Messages.authSuccess = value; break;
            case "not_registered": Messages.notRegistered = value; break;
            case "too_many_attempts": Messages.tooManyAttempts = value; break;
            case "took_too_long_to_login": Messages.tookTooLongToLogin = value; break;
            case "password_too_long": Messages.passwordTooLong = value; break;
            case "password_too_small": Messages.passwordTooSmall = value; break;
            case "session_login": Messages.sessionLogin = value; break;
            case "unreg_success": Messages.unRegSuccess = value; break;
            default: return false;
        }
        Authenticator.config.set(key, value);
        Authenticator.config.save();
        return true;
    }

    public static String getValue(String key) {
        return switch (key) {
            case "min_password_length" -> String.valueOf(Values.minPasswordLength);
            case "max_password_length" -> String.valueOf(Values.maxPasswordLength);
            case "max_login_attempts" -> String.valueOf(Values.maxLoginAttempts);
            case "login_timeout" -> String.valueOf(Values.loginTimeOut);
            case "save_interval" -> String.valueOf(Values.saveInterval);
            case "sessions" -> String.valueOf(Values.sessions);
            case "session_time" -> String.valueOf(Values.sessionTime);
            case "compress_database" -> String.valueOf(Values.compressDatabase);
            case "encryption_type" -> Values.encryptionType.name();
            default -> null;
        };
    }

    public static boolean setValue(String key, String value) {
        try {
            switch (key) {
                case "min_password_length":
                    Values.minPasswordLength = Integer.parseInt(value);
                    Authenticator.config.set(key, Values.minPasswordLength);
                    break;
                case "max_password_length":
                    Values.maxPasswordLength = Integer.parseInt(value);
                    Authenticator.config.set(key, Values.maxPasswordLength);
                    break;
                case "max_login_attempts":
                    Values.maxLoginAttempts = Integer.parseInt(value);
                    Authenticator.config.set(key, Values.maxLoginAttempts);
                    break;
                case "login_timeout":
                    Values.loginTimeOut = Integer.parseInt(value);
                    Authenticator.config.set(key, Values.loginTimeOut);
                    break;
                case "save_interval":
                    Values.saveInterval = Integer.parseInt(value);
                    Authenticator.config.set(key, Values.saveInterval);
                    break;
                case "sessions":
                    Values.sessions = parseBoolean(value);
                    Authenticator.config.set(key, Values.sessions);
                    break;
                case "session_time":
                    Values.sessionTime = Integer.parseInt(value);
                    Authenticator.config.set(key, Values.sessionTime);
                    break;
                case "compress_database":
                    Values.compressDatabase = parseBoolean(value);
                    Authenticator.config.set(key, Values.compressDatabase);
                    break;
                case "encryption_type":
                    Values.encryptionType = Cryptography.EncryptionType.valueOf(value.toUpperCase());
                    Authenticator.config.set(key, Values.encryptionType.name());
                    break;
                default:
                    return false;
            }
            Authenticator.config.save();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static boolean parseBoolean(String value) throws IllegalArgumentException {
        if (value.equalsIgnoreCase("true")) return true;
        else if (value.equalsIgnoreCase("false")) return false;
        throw new IllegalArgumentException();
    }
}
