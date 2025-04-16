package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Users;
import be.helha.poo3.serverpoo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Contrôleur gérant les opérations liées aux utilisateurs.
 * Fournit un point d'entrée pour ajouter un nouvel utilisateur.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * Ajoute un nouvel utilisateur en encodant son mot de passe
     * avant de l'enregistrer dans la base de données.
     *
     * @param user l'utilisateur à ajouter
     * @return l'utilisateur ajouté (avec son identifiant mis à jour)
     */
    @PostMapping
    public Users addUser(@RequestBody Users user) {
        // Encoder le mot de passe avant de le sauvegarder
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userService.addUser(user);
        return user;
    }
}
