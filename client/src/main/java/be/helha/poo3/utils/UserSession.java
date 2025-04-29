package be.helha.poo3.utils;

/**
 * Classe utilitaire statique qui gère les informations de session utilisateur.
 *
 * <p>
 * Elle stocke temporairement en mémoire :
 * <ul>
 *     <li>Le token d'accès (JWT) utilisé pour authentifier les appels API</li>
 *     <li>Le nom d'utilisateur de l'utilisateur connecté</li>
 * </ul>
 * </p>
 *
 * <p>
 * Cette classe est utilisée par {@link be.helha.poo3.services.AuthService}
 * pour sauvegarder les informations lors de la connexion et pour les réinitialiser à la déconnexion.
 * </p>
 *
 * <p><b>Important :</b> Les données sont stockées en mémoire et seront perdues à la fermeture de l'application.</p>
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

