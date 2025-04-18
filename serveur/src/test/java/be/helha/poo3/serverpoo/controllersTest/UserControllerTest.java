package be.helha.poo3.serverpoo.controllersTest;

import be.helha.poo3.serverpoo.controllers.UserController;
import be.helha.poo3.serverpoo.models.Users;
import be.helha.poo3.serverpoo.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de tests unitaires pour {@link UserController}.
 *
 * <p>
 * Utilise JUnit 5 et Mockito pour isoler la logique
 * du contrôleur en simulant les appels au {@link UserService}.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private Users existingUser;
    private Users newUser;

    @BeforeEach
    void setUp() {
        // Initialisation des objets Users utilisés dans les tests
        existingUser = new Users(1, "admin", "encodedPass", "USER", true);
        newUser = new Users(0, "newUser", "plainPass", null, false);
    }

    @Test
    void testGetUsers_Success() {
        // GIVEN
        List<Users> mockUsers = Arrays.asList(
                new Users(1, "alice", "passAlice", "USER", true),
                new Users(2, "bob", "passBob", "ADMIN", true)
        );
        when(userService.getUsers()).thenReturn(mockUsers);

        // WHEN
        List<Users> userList = userController.getUsers();

        // THEN
        verify(userService, times(1)).getUsers();
        assertEquals(2, userList.size());
        assertEquals("alice", userList.get(0).getUsername());
        assertEquals("bob", userList.get(1).getUsername());
    }

    @Test
    void testGetUserById_Success() {
        // GIVEN
        when(userService.getUserById(1)).thenReturn(existingUser);

        // WHEN
        Users result = userController.getUserById(1);

        // THEN
        verify(userService, times(1)).getUserById(1);
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        // GIVEN
        // Simule le fait que la DB ne trouve aucun user pour l’ID = 99
        doThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "L'utilisateur avec l'ID 99 n'existe pas."))
                .when(userService).getUserById(99);

        // WHEN & THEN
        // On s’attend à ce que l’exception ResponseStatusException soit propagée
        assertThrows(ResponseStatusException.class,
                () -> userController.getUserById(99));
    }

    @Test
    void testAddUser_Success() {
        // GIVEN
        // On simule la logique de service qui va renvoyer le nouvel utilisateur avec un ID généré
        Users insertedUser = new Users(10, "newUser", "encoded", "USER", true);
        when(userService.addUser(newUser)).thenReturn(insertedUser);

        // WHEN
        Users result = userController.addUser(newUser);

        // THEN
        verify(userService, times(1)).addUser(newUser);
        assertEquals(10, result.getId_user());
        assertEquals("newUser", result.getUsername());
        assertEquals("encoded", result.getPassword());
        assertEquals("USER", result.getRole());
        assertTrue(result.getActivated());
    }

    @Test
    void testDeleteUser_Success() {
        // WHEN
        userController.deleteUser(1);

        // THEN
        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    void testUpdateUser_Success() {
        // GIVEN
        Users updatedUser = new Users(1, "updatedUsername", "updatedPass", "ADMIN", true);
        when(userService.updateUser(eq(1), any(Users.class))).thenReturn(updatedUser);

        // WHEN
        Users controllerResult = userController.updateUser(1, updatedUser);

        // THEN
        verify(userService, times(1)).updateUser(eq(1), eq(updatedUser));
        assertEquals(1, controllerResult.getId_user());
        assertEquals("updatedUsername", controllerResult.getUsername());
        assertEquals("updatedPass", controllerResult.getPassword());
        assertEquals("ADMIN", controllerResult.getRole());
        assertTrue(controllerResult.getActivated());
    }
}