package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.models.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collections;

/**
 * Service permettant de gérer les utilisateurs en interagissant avec la base de données.
 * Cette classe implémente l'interface {@link UserDetailsService} de Spring Security
 * pour charger les informations d'un utilisateur depuis la base de données.
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private DataSource dataSource;

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
        String sql = "SELECT username, password, role FROM user WHERE username = ?";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
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
     * Ajoute un nouvel utilisateur dans la base de données et met à jour son identifiant.
     *
     * @param user l'utilisateur à ajouter
     * @throws RuntimeException en cas d'erreur lors de l'insertion en base de données
     */
    public void addUser(Users user) {
        String sql = "INSERT INTO user (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getRole());
                stmt.executeUpdate();

                // Permet de récupérer la clé générée (ID) pour le voir dans le response JSON
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion de l'utilisateur : ", e);
        }
    }
}