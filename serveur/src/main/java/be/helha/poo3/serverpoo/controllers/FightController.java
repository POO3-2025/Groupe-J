package be.helha.poo3.serverpoo.controllers;


import be.helha.poo3.serverpoo.exceptions.InventoryIOException;
import be.helha.poo3.serverpoo.models.CharacterWithPos;
import be.helha.poo3.serverpoo.models.PVMFight;
import be.helha.poo3.serverpoo.services.FightService;
import be.helha.poo3.serverpoo.services.InGameCharacterService;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la gestion des combats dans le jeu, principalement les combats Player vs Monster (PvM).
 * Ce contrôleur fournit des endpoints pour :r écupérer la liste des combats PvM en cours,créer un nouveau combat PvM pour un personnage authentifié,
 *effectuer une action durant un tour de combat (attaquer, esquiver, bloquer),terminer un combat et récupérer la récompense,
 *gérer la récupération ou le rejet du butin à la fin du combat.
 * Toutes les actions nécessitent un token JWT valide transmis via l'en-tête Authorization, sauf la récupération de la liste des combats PvM.

 */
@RestController
@RequestMapping("/fight")
public class FightController {

    @Autowired
    private InGameCharacterService inGameCharacterService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FightService fightService;

    /**
     * Récupère la liste des combats PvM (Player versus Monster) disponibles.
     *
     * @return ResponseEntity contenant la liste des combats PvM.
     */
    @GetMapping(path = "/pvm")
    public ResponseEntity<?> getPvm() {
        return ResponseEntity.ok(fightService.getPvmFights());
    }



    /**
     * Crée un nouveau combat PvM pour le personnage authentifié via le token JWT.
     *
     * @param authHeader L'en-tête HTTP Authorization contenant le token JWT (ex: "Bearer <token>").
     * @return ResponseEntity avec le combat PvM créé si succès,
     *         403 Forbidden si l'en-tête Authorization est manquant ou personnage non trouvé,
     *         401 Unauthorized si le token est invalide,
     *         500 Internal Server Error en cas d'erreur serveur.
     */
    @PostMapping(path = "/pvm")
    public ResponseEntity<?> createPvmFight(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Character not in game session");
            }
            return ResponseEntity.ok(fightService.createPvmFight(character.getIdCharacter()));

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }


    /**
     * Effectue une action durant un tour de combat PvM (attaque, esquive ou blocage).
     *
     * @param action    L'action à effectuer : "attack", "dodge" ou "block".
     * @param authHeader L'en-tête HTTP Authorization contenant le token JWT.
     * @return ResponseEntity avec le résultat du tour de combat si succès,
     *         400 Bad Request si l'action n'est pas valide,
     *         403 Forbidden si le personnage n'est pas trouvé ou l'en-tête manquant,
     *         401 Unauthorized si le token est invalide,
     *         500 Internal Server Error en cas d'erreur serveur.
     */
    @PostMapping("/pvm/{action}")
    public ResponseEntity<?> playTurn(@PathVariable String action, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            if (action.equals("dodge") || action.equals("attack") || action.equals("block")) {
                CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
                if (character == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Character not in game session");
                }
                PVMFight.PvmTurnResult result = fightService.playPvmTurn(character.getIdCharacter(), action);
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Action must be 'attack', 'dodge' or 'block'");

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Termine un combat PvM et retourne la récompense associée.
     *
     * @param authHeader L'en-tête HTTP Authorization contenant le token JWT.
     * @return ResponseEntity avec la récompense si succès,
     *         403 Forbidden si le personnage n'est pas trouvé ou l'en-tête manquant,
     *         401 Unauthorized si le token est invalide,
     *         500 Internal Server Error en cas d'erreur serveur.
     */
    @GetMapping("/pvm/end")
    public ResponseEntity<?> endFight(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Character not in game session");
            }
            return ResponseEntity.ok(fightService.getReward(character.getIdCharacter()));


        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Gère la fin du combat PvM avec une action sur le butin : le jeter ou le récupérer.
     *
     * @param action    L'action à effectuer : "throw" pour jeter le butin, "get" pour le récupérer.
     * @param authHeader L'en-tête HTTP Authorization contenant le token JWT.
     * @return ResponseEntity indiquant le succès de l'action,
     *         400 Bad Request si l'action n'est pas valide,
     *         403 Forbidden si le personnage n'est pas trouvé ou l'en-tête manquant,
     *         401 Unauthorized si le token est invalide,
     *         500 Internal Server Error en cas d'erreur serveur ou IOException liée à l'inventaire.
     */
    @PostMapping("/pvm/end/{action}")
    public ResponseEntity<?> endFight(@PathVariable String action, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        System.out.println(action);
        if (action.equals("throw") || action.equals("get")){
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            try {
                int tokenUserId = jwtUtils.getUserIdFromToken(token);
                CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
                if (character == null) throw new RuntimeException("Character not in game session");
                return ResponseEntity.ok(fightService.endPvmFight(character.getIdCharacter(),action.equals("get")));
            } catch (JwtException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
            } catch (InventoryIOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Action must be 'throw' or 'get'");
    }
}
