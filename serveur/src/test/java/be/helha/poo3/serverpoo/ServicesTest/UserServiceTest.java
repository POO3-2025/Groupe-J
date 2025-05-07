package be.helha.poo3.serverpoo.ServicesTest;

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

        // Spy pour pouvoir mocker uniquement certaines méthodes
        UserService spyService = Mockito.spy(userService);

        // Simuler que "testUser" n'existe PAS déjà
        doReturn(false).when(spyService).userExists("testUser");

        // Simuler l'encodage du mot de passe
        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("encodedPassword");

        // Simuler la préparation du Statement et l'exécution
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockPreparedStatement);

        // Simuler l'insertion générant un ID
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(99);

        // WHEN
        Users createdUser = spyService.addUser(newUser);

        // THEN
        verify(mockPreparedStatement).setString(1, "testUser");
        verify(mockPreparedStatement).setString(2, "encodedPassword");
        verify(mockPreparedStatement).setString(3, "USER"); // Default role
        verify(mockPreparedStatement).setBoolean(4, true); // Activated

        assertEquals(99, createdUser.getId_user());
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals("USER", createdUser.getRole());
        assertTrue(createdUser.getActivated());
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
        int authenticatedUserId = 10;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // 1 ligne affectée

        // WHEN
        userService.deleteUser(userIdToDelete, authenticatedUserId);

        // THEN
        verify(mockConnection).prepareStatement(contains("UPDATE user SET activated = false"));
        verify(mockPreparedStatement).setInt(1, userIdToDelete);
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testDeleteUser_NoRowsAffected() throws Exception {
        // GIVEN
        int userIdToDelete = 99;
        int authenticatedUserId = 99;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0); // Simule qu'aucune ligne affectée

        // WHEN & THEN
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(userIdToDelete, authenticatedUserId);
        });

        assertTrue(exception.getMessage().contains("Impossible de désactiver l'utilisateur"));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // GIVEN
        int userId = 5;
        Users existingUser = new Users(userId, "oldName", "oldPass", "USER", true);

        // Spy pour intercepter uniquement certaines méthodes
        UserService spyService = Mockito.spy(userService);

        doReturn(existingUser).when(spyService).getUserById(userId);
        doReturn(false).when(spyService).userExists("newName"); // Important : simuler que le nouveau username n'existe pas déjà

        // Mock de l'update SQL
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        // Préparation du nouvel utilisateur
        Users updatedInfo = new Users(0, "newName", "newPass", "ADMIN", true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        // WHEN
        Users result = spyService.updateUser(userId, updatedInfo);

        // THEN
        assertNotNull(result);
        assertEquals(userId, result.getId_user());
        assertEquals("newName", result.getUsername());
        assertEquals("encodedNewPass", result.getPassword());
        assertEquals("ADMIN", result.getRole());
        assertTrue(result.getActivated());
    }
}