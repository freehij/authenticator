package io.github.freehij.authenticator.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Cryptography {
    public static String hash(String name, String password) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest((name + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}