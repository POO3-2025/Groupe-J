package be.helha.poo3.serverpoo.utils;

/**
 * Réponse envoyée après une authentification réussie.
 * Contient les tokens JWT, un message, et l'ID utilisateur.
 */
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String message;
    private int id_user;

    public AuthenticationResponse(String accessToken, String refreshToken, String message, int id_user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
        this.id_user = id_user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
}
