package be.helha.poo3.serverpoo.controllersTest;

import be.helha.poo3.serverpoo.controllers.UserController;
import be.helha.poo3.serverpoo.models.Users;
import be.helha.poo3.serverpoo.services.UserService;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Classe de tests unitaires pour {@link UserController}.
 *
 * Utilise JUnit 5 et Mockito pour isoler la logique
 * du contrôleur en simulant les appels au {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserController userController;

    private Users existingUser;
    private Users newUser;

    @BeforeEach
    void setUp() {
        existingUser = new Users(1, "admin", "encodedPass", "USER", true,0);
        newUser = new Users(0, "newUser", "plainPass", null, false, 0);
    }

    @Test
    void testGetUsers_Success() {
        List<Users> mockUsers = Arrays.asList(
                new Users(1, "alice", "passAlice", "USER", true,0),
                new Users(2, "bob", "passBob", "ADMIN", true,0)
        );
        when(userService.getUsers()).thenReturn(mockUsers);

        List<Users> userList = userController.getUsers();

        verify(userService, times(1)).getUsers();
        assertEquals(2, userList.size());
        assertEquals("alice", userList.get(0).getUsername());
        assertEquals("bob", userList.get(1).getUsername());
    }

    @Test
    void testGetUserById_Success() {
        when(userService.getUserById(1)).thenReturn(existingUser);

        Users result = userController.getUserById(1);

        verify(userService, times(1)).getUserById(1);
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
                .when(userService).getUserById(99);

        assertThrows(ResponseStatusException.class,
                () -> userController.getUserById(99));
    }

    @Test
    void testAddUser_Success() {
        Users insertedUser = new Users(10, "newUser", "encoded", "USER", true, 0);
        when(userService.addUser(newUser)).thenReturn(insertedUser);

        Users result = userController.addUser(newUser);

        verify(userService, times(1)).addUser(newUser);
        assertEquals(10, result.getId_user());
        assertEquals("newUser", result.getUsername());
        assertEquals("encoded", result.getPassword());
        assertEquals("USER", result.getRole());
        assertTrue(result.getActivated());
    }

    @Test
    void testDeleteUser_Success() {
        int userId = 1;
        String tokenHeader = "Bearer faketoken";

        when(jwtUtils.getUserIdFromToken("faketoken")).thenReturn(userId);

        ResponseEntity<?> response = userController.deleteUser(userId, tokenHeader);

        verify(userService).deleteUser(userId, userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteUser_NoToken_Forbidden() {
        ResponseEntity<?> response = userController.deleteUser(1, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("connecté"));
    }

    @Test
    void testDeleteUser_InvalidToken_Unauthorized() {
        String badToken = "Bearer badtoken";

        when(jwtUtils.getUserIdFromToken("badtoken")).thenThrow(new io.jsonwebtoken.JwtException("Invalid Token"));

        ResponseEntity<?> response = userController.deleteUser(1, badToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Token invalide"));
    }

    @Test
    void testUpdateUser_WithToken_Success() {
        int userId = 1;
        String tokenHeader = "Bearer faketoken";
        Users updatedUser = new Users(userId, "updatedUsername", "updatedPass", "ADMIN", true, 0);

        when(jwtUtils.getUserIdFromToken("faketoken")).thenReturn(userId);
        when(userService.updateUser(eq(userId), any(Users.class))).thenReturn(updatedUser);

        ResponseEntity<?> response = userController.updateUser(tokenHeader, userId, updatedUser);

        verify(jwtUtils).getUserIdFromToken("faketoken");
        verify(userService).updateUser(eq(userId), eq(updatedUser));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
    }

    @Test
    void testUpdateUser_TokenUserMismatch_Forbidden() {
        int pathId = 2;
        String tokenHeader = "Bearer wrongtoken";

        when(jwtUtils.getUserIdFromToken("wrongtoken")).thenReturn(1);

        Users dummyUpdate = new Users(2, "other", "pass", "USER", true, 0);

        ResponseEntity<?> response = userController.updateUser(tokenHeader, pathId, dummyUpdate);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("propre compte"));
    }

    @Test
    void testUserExists_True() {
        when(userService.userExists("existingUser")).thenReturn(true);

        assertTrue(userController.userExists("existingUser"));
    }

    @Test
    void testUserExists_False() {
        when(userService.userExists("newUser")).thenReturn(false);

        assertFalse(userController.userExists("newUser"));
    }
}
