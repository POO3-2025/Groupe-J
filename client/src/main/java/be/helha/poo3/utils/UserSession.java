package be.helha.poo3.utils;

/**
 * Classe utilitaire statique qui gère les informations de session utilisateur.
 */
public class UserSession {
    private static String accessToken;
    private static String username;

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String token) {
        UserSession.accessToken = token;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        UserSession.username = username;
    }

    public static void clear() {
        accessToken = null;
        username = null;
    }
}

