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

