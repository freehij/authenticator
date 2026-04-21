package io.github.freehij.authenticator.util;

import io.github.freehij.authenticator.data.Values;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Cryptography {
    public static String hash(String name, String password) {
        return hash(name, password, Values.encryptionType);
    }

    public static String hash(String name, String password, EncryptionType algorithm) {
        try {
            byte[] hash = MessageDigest.getInstance(algorithm.name)
                    .digest((name + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum EncryptionType {
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256"),
        SHA384("SHA-384"),
        SHA512("SHA-512");

        final String name;

        EncryptionType(String name) {
            this.name = name;
        }
    }
}