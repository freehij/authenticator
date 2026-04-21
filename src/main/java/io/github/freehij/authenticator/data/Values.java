package io.github.freehij.authenticator.data;

import io.github.freehij.authenticator.util.Cryptography;

public class Values {
    public static int minPasswordLength, maxPasswordLength, maxLoginAttempts, loginTimeOut, saveInterval;
    public static boolean sessions, compressDatabase = true;
    public static long sessionTime;
    public static Cryptography.EncryptionType encryptionType = Cryptography.EncryptionType.SHA256;
}
