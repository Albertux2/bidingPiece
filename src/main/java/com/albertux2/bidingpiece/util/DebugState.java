package com.albertux2.bidingpiece.util;

import java.util.HashSet;
import java.util.Set;

public class DebugState {
    private static final Set<String> enabledPlayers = new HashSet<>();

    private static boolean serverDebug = false;

    public static boolean isDebugEnabled(String playerName) {
        return serverDebug || enabledPlayers.contains(playerName);
    }

    public static void setDebugEnabled(String playerName, boolean enabled) {
        if ("server".equalsIgnoreCase(playerName)) {
            serverDebug = enabled;
        } else {
            if (enabled) {
                enabledPlayers.add(playerName);
            } else {
                enabledPlayers.remove(playerName);
            }
        }
    }

    public static void clear() {
        enabledPlayers.clear();
        serverDebug = false;
    }
}
