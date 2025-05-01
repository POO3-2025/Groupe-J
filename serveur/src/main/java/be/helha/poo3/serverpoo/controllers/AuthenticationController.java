package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Users;
import be.helha.poo3.serverpoo.services.UserService;
import be.helha.poo3.serverpoo.utils.AuthenticationResponse;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import be.helha.poo3.serverpoo.utils.RefreshRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur gérant l'authentification des utilisateurs.
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Users user = userService.getUserByUsername(loginRequest.getUsername());

            String accessToken = jwtUtils.generateToken(authentication, user.getId_user());
            String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

            // Retourne également id_user dans la réponse
            return ResponseEntity.ok(new AuthenticationResponse(accessToken, refreshToken, "Authentification réussie !", user.getId_user()));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Échec de l'authentification");
        }
    }

    /**
     * Endpoint pour rafraîchir un access token via un refresh token valide.
     *
     * @param request Contient le refresh token.
     * @return Nouveau access token et informations utilisateur.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalide ou expiré");
        }

        String username = jwtUtils.getUsernameFromRefreshToken(refreshToken);
        Users user = userService.getUserByUsername(username);

        String newAccessToken = jwtUtils.generateTokenFromUser(user);
        refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        return ResponseEntity.ok(new AuthenticationResponse(newAccessToken, refreshToken, "Nouveau token généré", user.getId_user()));
    }

    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
