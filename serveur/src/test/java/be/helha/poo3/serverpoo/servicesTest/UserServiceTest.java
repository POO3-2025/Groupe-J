package be.helha.poo3.serverpoo.servicesTest;

import be.helha.poo3.serverpoo.models.Users;
import be.helha.poo3.serverpoo.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de tests unitaires pour la classe {@link UserService}.
 *
 * <p>Utilise JUnit 5 et Mockito pour isoler la logique métier
 * en simulant les interactions avec la base de données (DataSource)
 * et l'encodeur de mot de passe (PasswordEncoder).</p>
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    // Mocks (objets simulés) pour l'injection
    @Mock
    private DataSource dataSource;

    @Mock
    private PasswordEncoder passwordEncoder;

    // Injecte les mocks dans l'instance de UserService
    @InjectMocks
    private UserService userService;

    // Mocks SQL fréquemment utilisés
    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        // Par défaut, on suppose que dataSource.getConnection() renvoie mockConnection
        when(dataSource.getConnection()).thenReturn(mockConnection);
    }

    @Test
    void testAddUser_Success() throws Exception {
        // GIVEN
        Users newUser = new Users(0, "testUser", "plainPassword", null, false);

        // On simule un mot de passe encodé
        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");

        // On simule la création du PreparedStatement & l'exécution de la requête
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Simule l'insertion qui génère un ID 99
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(99);

        // WHEN
        Users createdUser = userService.addUser(newUser);

        // THEN
        verify(mockPreparedStatement).setString(1, "testUser");
        verify(mockPreparedStatement).setString(2, "encodedPassword");
        // Role doit être "USER" par défaut si null
        verify(mockPreparedStatement).setString(3, "USER");
        // Le compte doit être activé
        verify(mockPreparedStatement).setBoolean(4, true);

        // Vérifications sur le résultat
        assertEquals(99, createdUser.getId_user());
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals("USER", createdUser.getRole());
        assertTrue(createdUser.getActivated(), "L'utilisateur doit être activé par défaut");
    }

    @Test
    void testGetUserByUsername_UserFoundAndActivated() throws Exception {
        // GIVEN
        String username = "admin";
        String passwordEncoded = "encodedPass";
        String role = "ADMIN";

        // On simule le PreparedStatement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // On simule le paramètre username
        doNothing().when(mockPreparedStatement).setString(eq(1), eq(username));

        // On simule le résultat
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);  // Indique qu'on a trouvé un enregistrement

        // On simule les colonnes
        when(mockResultSet.getString("username")).thenReturn(username);
        when(mockResultSet.getString("password")).thenReturn(passwordEncoded);
        when(mockResultSet.getString("role")).thenReturn(role);
        when(mockResultSet.getBoolean("activated")).thenReturn(true);

        // WHEN
        var userDetails = userService.loadUserByUsername(username);

        // THEN
        assertEquals(username, userDetails.getUsername());
        assertEquals(passwordEncoded, userDetails.getPassword());
        // Vérifie que l'autorité est prefixée par "ROLE_"
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role)));
    }

    @Test
    void testGetUserByUsername_UserNotFound() throws Exception {
        // GIVEN
        String username = "unknownUser";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        // Simule le fait qu'on n'a aucun résultat (rs.next() = false)
        when(mockResultSet.next()).thenReturn(false);

        // WHEN & THEN
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username));
    }

    @Test
    void testGetUserByUsername_UserDisabled() throws Exception {
        // GIVEN
        String username = "disabledUser";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // On simule un enregistrement, mais "activated" = false
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getBoolean("activated")).thenReturn(false);

        // WHEN & THEN
        assertThrows(DisabledException.class,
                () -> userService.loadUserByUsername(username));
    }

    @Test
    void testGetUsers_ReturnsListOfUsers() throws Exception {
        // GIVEN
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Simule 2 utilisateurs
        when(mockResultSet.next()).thenReturn(true, true, false); // 2 lignes, puis stop
        when(mockResultSet.getInt("id_user")).thenReturn(1, 2);
        when(mockResultSet.getString("username")).thenReturn("Alice", "Bob");
        when(mockResultSet.getString("password")).thenReturn("encPassAlice", "encPassBob");
        when(mockResultSet.getString("role")).thenReturn("USER", "ADMIN");
        when(mockResultSet.getBoolean("activated")).thenReturn(true, true);

        // WHEN
        List<Users> userList = userService.getUsers();

        // THEN
        assertEquals(2, userList.size());
        assertEquals("Alice", userList.get(0).getUsername());
        assertEquals("encPassAlice", userList.get(0).getPassword());
        assertEquals("Bob", userList.get(1).getUsername());
        assertEquals("encPassBob", userList.get(1).getPassword());
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // GIVEN
        int userIdToDelete = 10;

        // Mock pour le prepareStatement avec un simple "executeUpdate()"
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 ligne affectée

        // WHEN
        userService.deleteUser(userIdToDelete);

        // THEN
        verify(mockConnection).prepareStatement(
                contains("UPDATE user SET activated = false"), eq(Statement.RETURN_GENERATED_KEYS)
        );
        verify(mockPreparedStatement).setInt(1, userIdToDelete);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDeleteUser_NoRowsAffected() throws Exception {
        // GIVEN
        int userIdToDelete = 99;
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        // Simule qu'aucune ligne n'a été affectée
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        // WHEN & THEN
        userService.deleteUser(userIdToDelete);
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // GIVEN
        int userId = 5;
        // On mock getUserById(...) pour renvoyer un user existant
        Users existingUser = new Users(userId, "oldName", "oldPass", "USER", true);

        // On peut "espionner" (spy) userService pour faire un "when(...).thenReturn(existingUser)".

        UserService spyService = Mockito.spy(userService);
        doReturn(existingUser).when(spyService).getUserById(userId);

        // Pour l'update, on simule l'exécution
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Nouvel user
        Users updatedInfo = new Users(0, "newName", "newPass", "ADMIN", true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        // WHEN
        Users result = spyService.updateUser(userId, updatedInfo);

        // THEN
        // Vérification
        assertNotNull(result);
        assertEquals(userId, result.getId_user());    // l'ID ne devrait pas changer
        assertEquals("newName", result.getUsername());
        assertEquals("encodedNewPass", result.getPassword());
        assertEquals("ADMIN", result.getRole());
        assertTrue(result.getActivated());
    }
}