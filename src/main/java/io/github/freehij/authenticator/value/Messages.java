package io.github.freehij.authenticator.value;

public class Messages {
    public static String register, login, alreadyRegistered, wrongPass, notLoggedIn, authSuccess, notRegistered,
            tooManyAttempts, tookTooLongToLogin, passwordTooLong, passwordTooSmall = "";

    public static String replaceSpecialSequence(String original, Object replacement) {
        return original.replace("%s", replacement.toString());
    }
}
