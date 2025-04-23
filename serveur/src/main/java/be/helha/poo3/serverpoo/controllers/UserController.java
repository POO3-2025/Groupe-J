package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Users;
import be.helha.poo3.serverpoo.services.UserService;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private JwtUtils jwtUtils;

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
     * Met à jour les informations d'un utilisateur spécifique, à condition que l'utilisateur
     * authentifié via le token JWT soit bien celui correspondant à l'ID dans l'URL.
     *
     * <p>Cette méthode vérifie d'abord la présence d'un en-tête Authorization contenant un token JWT
     * valide. Ensuite, elle extrait l'ID utilisateur du token et le compare à l'ID fourni dans
     * le chemin de la requête pour s'assurer que l'utilisateur ne peut modifier que ses propres données.</p>
     *
     * @param authHeader l'en-tête HTTP "Authorization" contenant le token JWT (de la forme "Bearer {token}")
     * @param id_user l'identifiant de l'utilisateur à modifier (extrait de l'URL)
     * @param userToUpdate les nouvelles données à appliquer à l'utilisateur
     * @return une réponse HTTP contenant l'utilisateur mis à jour en cas de succès,
     *         ou un message d'erreur approprié avec le code HTTP correspondant :
     *         <ul>
     *             <li><b>403 Forbidden</b> : si l'en-tête est manquant ou que l'utilisateur tente de modifier un autre compte</li>
     *             <li><b>401 Unauthorized</b> : si le token est invalide</li>
     *             <li><b>500 Internal Server Error</b> : en cas d'erreur lors de la mise à jour</li>
     *         </ul>
     */

    @PutMapping(path="/{id_user}")
    public ResponseEntity<?> updateUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable int id_user,
            @RequestBody Users userToUpdate
    ) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);

            // Vérifie que l'utilisateur du token est bien celui qu'on essaie de modifier
            if (tokenUserId != id_user) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous ne pouvez modifier que votre propre compte.");
            }

            Users updatedUser = userService.updateUser(id_user, userToUpdate);
            return ResponseEntity.ok(updatedUser);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

}