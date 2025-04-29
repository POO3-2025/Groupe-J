package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.models.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service permettant de gérer les utilisateurs en interagissant avec la base de données.
 * Cette classe implémente l'interface {@link UserDetailsService} de Spring Security
 * pour charger les informations d'un utilisateur depuis la base de données.
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Charge les informations d'un utilisateur à partir de son nom d'utilisateur.
     * Cette méthode est utilisée par Spring Security pour l'authentification.
     *
     * @param username le nom d'utilisateur à rechercher
     * @return les détails de l'utilisateur correspondant
     * @throws UsernameNotFoundException si l'utilisateur n'est pas trouvé
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT username, password, role, activated FROM user WHERE username = ?";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {

                        // Vérification du statut d'activation du compte
                        boolean activated = rs.getBoolean("activated");
                        if (!activated) {
                            throw new DisabledException("Le compte de l'utilisateur n'est pas activé.");
                        }

                        return org.springframework.security.core.userdetails.User
                                .builder()
                                .username(rs.getString("username"))
                                .password(rs.getString("password"))
                                .authorities(Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_" + rs.getString("role"))))
                                .build();
                    } else {
                        throw new UsernameNotFoundException("Utilisateur non trouvé : " + username);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de l'utilisateur", e);
        }
    }

    /**
     * Récupère la liste de tous les utilisateurs dont le compte est activé à vraie (activated = true).
     *
     * @return une liste d'objets {@code Users} actifs (activated = true)
     * @throws RuntimeException si une exception SQL se produit lors de l'exécution de la requête
     */
    public List<Users> getUsers() {
        String sql = "SELECT * FROM user WHERE activated = true";

        List<Users> userList = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Users user = new Users(
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("activated")
                );
                userList.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs : ", e);
        }
        return userList;
    }

    /**
     * Récupère l'utilisateur correspondant à l'ID spécifié dans la base de données.
     *
     * @param id_user l'ID de l'utilisateur à rechercher
     * @return un objet Users correspondant à l'ID, ou null si aucun utilisateur n'est trouvé
     * @throws RuntimeException si une erreur survient lors de l'exécution de la requête SQL
     */
    public Users getUserById(int id_user) {
        String sql = "SELECT * FROM user WHERE id_user = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

             stmt.setInt(1, id_user);

             try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Users user = new Users(
                            rs.getInt("id_user"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getBoolean("activated")
                    );
                    return user;
                } else {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "L'utilisateur avec l'ID " + id_user + " n'existe pas.");
                }
             }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de l'utilisateur : ", e);
        }
    }

    /**
     * Recherche un utilisateur en base de données à partir de son nom d'utilisateur.
     *
     * @param username le nom d'utilisateur à chercher
     * @return l'utilisateur correspondant
     * @throws ResponseStatusException si aucun utilisateur n'est trouvé
     * @throws RuntimeException en cas d'erreur lors de l'accès à la base de données
     */
    public Users getUserByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Users(
                            rs.getInt("id_user"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getBoolean("activated")
                    );
                } else {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé avec le username : " + username);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de l'utilisateur par username", e);
        }
    }


    /**
     * Ajoute un nouvel utilisateur dans la base de données et met à jour son identifiant auto-généré.
     * L'utilisateur est automatiquement activé et son mot de passe encodé avant l'insertion.
     *
     * @param user l'utilisateur à ajouter
     * @return l'objet {@code Users} inséré, avec son nouvel ID mis à jour
     * @throws RuntimeException en cas d'erreur lors de l'insertion en base de données
     */
    public Users addUser(Users user) {
        // Avant d'insérer, vérifier si le username existe déjà
        if (userExists(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nom d'utilisateur existe déjà.");
        }

        String sql = "INSERT INTO user (username, password, role, activated) VALUES (?, ?, ?, ?)";

        user.setActivated(true);

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("USER");
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setBoolean(4, user.getActivated());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId_user(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion de l'utilisateur : ", e);
        }

        return user;
    }

    /**
     * Désactive l'utilisateur dont l'ID est spécifié en mettant son champ "activated" à false dans la base de données.
     *
     * @param id_user L'ID de l'utilisateur à désactiver.
     * @param authenticatedUserId L'ID de l'utilisateur authentifié extrait du token JWT.
     * @throws ResponseStatusException si l'utilisateur tente de supprimer un compte qui n'est pas le sien.
     * @throws RuntimeException en cas d'erreur SQL ou si l'utilisateur n'a pas pu être désactivé.
     */
    public void deleteUser(int id_user, int authenticatedUserId) {
        // Vérifie que l'utilisateur connecté correspond à celui qu'on veut supprimer
        if (id_user != authenticatedUserId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas autorisé à supprimer cet utilisateur.");
        }

        String sql = "UPDATE user SET activated = false WHERE id_user = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id_user);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Impossible de désactiver l'utilisateur d'ID " + id_user);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la désactivation de l'utilisateur : ", e);
        }
    }

    /**
     * Met à jour un utilisateur existant en base de données.
     * Seuls les champs non-nuls dans userData sont pris en compte.
     *
     * @param id_user   l'ID de l'utilisateur à modifier
     * @param userToAdd les nouvelles valeurs (partielles ou complètes) pour l'utilisateur
     * @return l'utilisateur final mis à jour
     * @throws RuntimeException si l'utilisateur à modifier n'existe pas
     */
    public Users updateUser(int id_user, Users userToAdd) {
        // Récupération de l'utilisateur existant
        Users existingUser = getUserById(id_user);
        if (existingUser == null) {
            throw new RuntimeException("Aucun utilisateur trouvé avec l'ID " + id_user);
        }

        // Mise à jour des champs
        if (userToAdd.getUsername() != null) {
            // Vérifier si le nouveau nom d'utilisateur est différent et déjà pris
            if (!existingUser.getUsername().equals(userToAdd.getUsername()) && userExists(userToAdd.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nom d'utilisateur existe déjà.");
            }
            existingUser.setUsername(userToAdd.getUsername());
        }
        if (userToAdd.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(userToAdd.getPassword()); // Encoder le nouveau mot de passe
            existingUser.setPassword(encodedPassword);
        }
        if (userToAdd.getRole() != null) {
            existingUser.setRole(userToAdd.getRole());
        }

        // Mise à jour dans la BD
        String sql = "UPDATE user SET username = ?, password = ?, role = ?, activated = ? WHERE id_user = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, existingUser.getUsername());
            stmt.setString(2, existingUser.getPassword());
            stmt.setString(3, existingUser.getRole());
            stmt.setBoolean(4, existingUser.getActivated());
            stmt.setInt(5, id_user);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Impossible de mettre à jour l'utilisateur d'ID " + id_user);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur : ", e);
        }

        return existingUser;
    }

    /**
     * Vérifie si un utilisateur avec le nom d'utilisateur spécifié existe déjà dans la base de données.
     *
     * Cette méthode exécute une requête SQL comptant le nombre d'entrées correspondant au nom d'utilisateur donné.
     * Elle est utilisée notamment avant une création de compte pour éviter les doublons.
     *
     * @param username Le nom d'utilisateur à vérifier.
     * @return {@code true} si un utilisateur avec ce nom existe, {@code false} sinon.
     * @throws RuntimeException si une erreur SQL survient pendant la requête.
     */
    public boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du nom d'utilisateur", e);
        }
        return false;
    }
}