package io.github.freehij.authenticator.util;

import io.github.freehij.authenticator.data.Values;

import java.util.HashMap;
import java.util.Map;

public class Sessions {
    static final Map<String, SessionEntry> nameToEntry = new HashMap<>();

    static class SessionEntry {
        String lastIP;
        long lastLogin;

        public SessionEntry(String lastIP, long lastLogin) {
            this.lastIP = lastIP;
            this.lastLogin = lastLogin;
        }
    }

    public static void updateSession(String name, String IP) {
        SessionEntry entry = nameToEntry.computeIfAbsent(name, k -> new SessionEntry(IP, 0));
        entry.lastIP = IP;
        entry.lastLogin = System.currentTimeMillis();
    }

    public static boolean checkSession(String name, String IP) {
        SessionEntry entry = nameToEntry.get(name);
        if (entry == null || entry.lastIP == null || !entry.lastIP.equals(IP)) return false;
        return isTimedOut(entry.lastLogin);
    }

    static boolean isTimedOut(long lastLogin) {
        return (System.currentTimeMillis() - lastLogin) < Values.sessionTime * 1000L;
    }

    public static void eraseSession(String name) {
        synchronized (nameToEntry) {
            nameToEntry.remove(name);
        }
    }
}
