package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Users;
import be.helha.poo3.serverpoo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Contrôleur gérant les opérations liées aux utilisateurs.
 * Fournit un point d'entrée pour ajouter un nouvel utilisateur.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Récupère la liste de tous les utilisateurs.
     *
     * @return une liste d'objets {@code Users}
     */
    @GetMapping
    public List<Users> getUsers() {
        List<Users> userList = userService.getUsers();
        return userList;
    }

    /**
     * Récupère un utilisateur à partir de son identifiant unique.
     *
     * @param id_user l'identifiant de l'utilisateur à rechercher
     * @return l'utilisateur correspondant à l'ID, ou {@code null} s'il n'existe pas
     */
    @GetMapping(path="/{id_user}")
    public Users getUserById(@PathVariable int id_user) {
        Users user = userService.getUserById(id_user);
        return user;
    }

    /**
     * Ajoute un nouvel utilisateur en encodant son mot de passe
     * avant de l'enregistrer dans la base de données.
     *
     * @param user l'utilisateur à ajouter
     * @return l'utilisateur ajouté (avec son identifiant mis à jour)
     */
    @PostMapping
    public Users addUser(@RequestBody Users user) {
        return userService.addUser(user);
        //return user;
    }

    /**
     * Supprime un utilisateur de manière logique
     * via son ID dans la base de données.
     *
     * @param id_user l'id de l'utilisateur à supprimer
     */
    @DeleteMapping(path="/{id_user}")
    public void deleteUser(@PathVariable int id_user) {
        userService.deleteUser(id_user);
    }

    /**
     * Met à jour un utilisateur existant dans la base à partir de l'ID fourni dans l'URL
     * et des nouvelles informations passées dans le corps de la requête.
     *
     * @param id_user l'identifiant unique de l'utilisateur à modifier
     * @param user    un objet Users contenant les champs à mettre à jour
     * @return l'utilisateur modifié avec ses nouvelles valeurs
     */
    @PutMapping(path="/{id_user}")
    public Users updateUser(@PathVariable int id_user, @RequestBody Users user) {
        return userService.updateUser(id_user, user);
    }
}