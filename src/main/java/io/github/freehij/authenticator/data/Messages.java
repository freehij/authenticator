package io.github.freehij.authenticator.data;

public class Messages {
    public static String register, login, alreadyRegistered, wrongPass, notLoggedIn, authSuccess, notRegistered,
            tooManyAttempts, tookTooLongToLogin, passwordTooLong, passwordTooSmall, sessionLogin, unRegSuccess;

    public static String replaceSpecialSequence(String original, Object replacement) {
        return original.replace("%s", replacement.toString());
    }
}
