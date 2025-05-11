package be.helha.poo3.serverpoo.controllers;


import be.helha.poo3.serverpoo.models.GameCharacter;
import be.helha.poo3.serverpoo.models.Users;
import be.helha.poo3.serverpoo.services.CharacterService;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/character")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping(path="/{id}")
    public GameCharacter getCharacter(
            @PathVariable int id
    ) {
        return characterService.getCharacterById(id);
    }

    @PostMapping
    public ResponseEntity<?> addCharacter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody GameCharacter character) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            if(character.getConstitution()+ character.getDexterity()+character.getStrength()>5) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("le nombre de point attribué est suppérieur à 5");
            int tokenUserId = jwtUtils.getUserIdFromToken(token);

            character.setIdUser(tokenUserId);
            character.setCurrentHP(100);
            character.setMaxHP(100);
            character = characterService.addCharacter(character);
            return ResponseEntity.ok(character);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }

    }


}
