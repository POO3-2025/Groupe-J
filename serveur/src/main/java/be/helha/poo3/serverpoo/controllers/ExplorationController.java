package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.CharacterWithPos;
import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.models.Room;
import be.helha.poo3.serverpoo.models.RoomDTO;
import be.helha.poo3.serverpoo.services.ExplorationService;
import be.helha.poo3.serverpoo.services.InGameCharacterService;
import be.helha.poo3.serverpoo.services.UserService;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des actions liées à l'exploration dans le jeu.
 * Ce contrôleur fournit des endpoints pour récupérer la salle actuelle du personnage,déplacer le personnage dans une direction donnée,
 *ouvrir un coffre dans la salle actuelle et récupérer le butin d'un coffre ouvert.
 * Toutes les actions nécessitent un token JWT valide transmis via l'en-tête Authorization.
 */
@RestController
@RequestMapping("/exploration")
public class ExplorationController {

    @Autowired
    private ExplorationService explorationService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private InGameCharacterService characterService;

    /**
     * Récupère la salle actuelle dans laquelle se trouve le personnage de l'utilisateur authentifié.
     *
     * @param authHeader l'en-tête HTTP Authorization contenant le token JWT (ex: "Bearer <token>")
     * @return ResponseEntity avec la salle actuelle sous forme d'objet RoomDTO si authentification réussie,
     *         ou une réponse 401 Unauthorized avec un message d'erreur si le token est invalide.
     */
    @GetMapping("/room")
    public ResponseEntity<?> getCurrentRoom(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = authHeader.substring(7);

        try {
            int authenticatedUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = (characterService.getInGameCharacterByUserId(authenticatedUserId));
            return ResponseEntity.ok(explorationService.getCurrentRoom(character));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token invalide"));
        }

    }

    /**
     * Déplace le personnage de l'utilisateur authentifié dans une direction donnée.
     *
     * @param direction la direction du déplacement (ex: "north", "south", "east", "west")
     * @param authHeader l'en-tête HTTP Authorization contenant le token JWT (ex: "Bearer <token>")
     * @return ResponseEntity avec la nouvelle salle sous forme de RoomDTO si déplacement réussi,
     *         une réponse 400 Bad Request si la salle est inaccessible,
     *         ou une réponse 401 Unauthorized si le token est invalide ou le personnage introuvable.
     */
    @PostMapping("/move/{direction}")
    public ResponseEntity<?> move(@PathVariable String direction,@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = authHeader.substring(7);

        try {
            int authenticatedUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = characterService.getInGameCharacterByUserId(authenticatedUserId);
            if(character == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Room newRoom = explorationService.move(character, direction);
            if(newRoom == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Salle inaccessible");
            } else {
                character.setPosition(new Point(newRoom.getX(), newRoom.getY()));
                return ResponseEntity.status(HttpStatus.OK).body(new RoomDTO(newRoom));
            }

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token invalide"));
        }

    }

    /**
     * Ouvre un coffre dans la salle actuelle du personnage de l'utilisateur authentifié.
     *
     * @param authHeader l'en-tête HTTP Authorization contenant le token JWT (ex: "Bearer <token>")
     * @return ResponseEntity avec l'objet Item contenu dans le coffre si ouverture réussie,
     *         une réponse 400 Bad Request si aucun coffre n'est présent,
     *         ou une réponse 401 Unauthorized si le token est invalide ou le personnage introuvable.
     */
    @GetMapping("/openChest")
    public ResponseEntity<?> openChest(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = authHeader.substring(7);

        try {
            int userId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = characterService.getInGameCharacterByUserId(userId);

            if (character == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Item item = explorationService.openChest(character);

            if (item == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Il n'y a pas de coffre à ouvrir ici");
            }

            return ResponseEntity.ok(item);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token invalide"));
        }
    }

    /**
     * Récupère le butin du coffre ouvert par le personnage de l'utilisateur authentifié.
     *
     * @param authHeader l'en-tête HTTP Authorization contenant le token JWT (ex: "Bearer <token>")
     * @return ResponseEntity avec un booléen indiquant si le loot a été récupéré avec succès,
     *         ou une réponse 401 Unauthorized si le token est invalide ou le personnage introuvable.
     */
    @PostMapping("/getLootFromChest")
    public ResponseEntity<?> getLootFromChest(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = authHeader.substring(7);

        try {
            int userId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = characterService.getInGameCharacterByUserId(userId);

            if (character == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            boolean lootSuccess = explorationService.getLootFromChest(character);

            return ResponseEntity.ok(lootSuccess);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token invalide"));
        }
    }
}

