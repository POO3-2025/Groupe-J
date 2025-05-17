package be.helha.poo3.serverpoo.models;

/**
 * Cette classe représente un utilisateur.
 */
public class Users {

    private int id_user;
    private String username;
    private String password;

    /**
     * Rôle de l'utilisateur (par exemple, ADMIN ou USER).
     */
    private String role;
    private boolean activated;
    private int idLastCharacter;

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
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

    public boolean getActivated() {
        return this.activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public int getIdLastCharacter() {
        return idLastCharacter;
    }

    public void setIdLastCharacter(int idIdLastCharacter) {
        this.idLastCharacter = idIdLastCharacter;
    }

    /**
     * Constructeur de la classe Users.
     *
     * @param id_user   l'identifiant unique
     * @param username  le nom d'utilisateur
     * @param password  le mot de passe
     * @param role      le rôle de l'utilisateur
     * @param activated le statut d'activation de l'utilisateur
     */
    public Users(int id_user, String username, String password, String role, boolean activated, int idLastCharacter) {
        this.id_user = id_user;
        this.username = username;
        this.password = password;
        this.role = role;
        this.activated = activated;
        this.idLastCharacter = idLastCharacter;
    }
}