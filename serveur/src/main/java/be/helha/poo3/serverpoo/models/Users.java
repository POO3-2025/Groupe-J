package be.helha.poo3.serverpoo.models;

/**
 * Cette classe représente un utilisateur.
 */
public class Users {

    private int id;
    private String username;
    private String password;

    /**
     * Rôle de l'utilisateur (par exemple, ADMIN ou USER).
     */
    private String role;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Constructeur de la classe Users.
     *
     * @param id        l'identifiant unique
     * @param username  le nom d'utilisateur
     * @param password  le mot de passe
     * @param role      le rôle de l'utilisateur
     */
    public Users(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
