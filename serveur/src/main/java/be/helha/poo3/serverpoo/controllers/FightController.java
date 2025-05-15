package be.helha.poo3.serverpoo.controllers;


import be.helha.poo3.serverpoo.exceptions.InventoryIOException;
import be.helha.poo3.serverpoo.models.CharacterWithPos;
import be.helha.poo3.serverpoo.services.FightService;
import be.helha.poo3.serverpoo.services.InGameCharacterService;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fight")
public class FightController {

    @Autowired
    private InGameCharacterService inGameCharacterService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private FightService fightService;


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
                return ResponseEntity.ok(fightService.playPvmTurn(character.getIdCharacter(), action));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Action must be 'attack', 'dodge' or 'block'");

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

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

    @PostMapping("/pvm/end/{action}")
    public ResponseEntity<?> endFight(@PathVariable String action, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        if (action.equals("throw") || action.equals("get")){
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            try {
                int tokenUserId = jwtUtils.getUserIdFromToken(token);
                CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
                if (character == null) throw new RuntimeException("Character not in game session");
                fightService.endPvmFight(character.getIdCharacter(),action.equals("get"));
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
