package be.helha.poo3.serverpoo.controllers;


import be.helha.poo3.serverpoo.models.CharacterCreationDTO;
import be.helha.poo3.serverpoo.models.CharacterWithPos;
import be.helha.poo3.serverpoo.models.GameCharacter;
import be.helha.poo3.serverpoo.services.CharacterService;
import be.helha.poo3.serverpoo.services.InGameCharacterService;
import be.helha.poo3.serverpoo.services.UserService;
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
    private UserService userService;

    @Autowired
    private InGameCharacterService inGameCharacterService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping(path="/{id}")
    public GameCharacter getCharacter(
            @PathVariable int id
    ) {
        return characterService.getCharacterById(id);
    }

    @GetMapping(path = "/myCharacters")
    public ResponseEntity<?> getMyCharacters(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println(authHeader);
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            return ResponseEntity.ok(characterService.getCharactersByUser(tokenUserId));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @GetMapping(path = "/myCharacter")
    public ResponseEntity<?> getMyCharacter(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println(authHeader);
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            return ResponseEntity.ok(inGameCharacterService.getInGameCharacterByUserId(tokenUserId));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @GetMapping(path = "/lastCharacter/{id}")
    public ResponseEntity<?> getLastCharacter(@PathVariable int id) {
        try {
            return ResponseEntity.ok(inGameCharacterService.getLastCharacter(id));
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @PostMapping
    public ResponseEntity<?> addCharacter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CharacterCreationDTO characterDTO) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            if(characterDTO.getConstitution()+ characterDTO.getDexterity()+characterDTO.getStrength()>5) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("le nombre de point attribué est suppérieur à 5");
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            GameCharacter character = new GameCharacter(0,tokenUserId,characterDTO.getName(),null,100,100,characterDTO.getConstitution(),characterDTO.getDexterity(),characterDTO.getStrength());
            character = characterService.addCharacter(character, characterDTO.getClasse());
            return ResponseEntity.ok(character);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }

    }

        @GetMapping(path = "/inGameCharacters")
    public ResponseEntity<?> getInGameCharacter(){
        return ResponseEntity.ok(inGameCharacterService.getLoadedCharacters());
    }

    @PostMapping(path = "/choice/{id}")
    public ResponseEntity<?> choiceCharacter(@RequestHeader(value = "Authorization", required = false) String authHeader,@PathVariable int id) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            if(characterService.characterExistsById(id)){
                CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
                if (character == null) {
                    userService.updateLastCharacter(tokenUserId,id);
                    GameCharacter newCharacter = characterService.getCharacterById(id);
                    return ResponseEntity.ok(inGameCharacterService.addCharacterInGame(newCharacter));
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous avez déjà un personnage en jeu");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Personnage inexistant");
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        }
    }

    @DeleteMapping(path = "/leave")
    public ResponseEntity<?> leaveCharacter(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            GameCharacter character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            if(character != null){
                if (inGameCharacterService.removeCharacterFromGame(character.getIdCharacter())){
                    return ResponseEntity.ok("sortie effectuée");
                }
                return ResponseEntity.ok("erreur lors de la sortie du personnage avec l'id "+ character.getIdCharacter() );
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Character inexistant");
        }catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        }
    }


    @PutMapping(path="/{id}")
    public ResponseEntity<?> changeCharacterName(
            @PathVariable int id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody RenameDto dto) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            if (characterService.userOwnsCharacter(tokenUserId,id)){
                characterService.updateCharacterName(id,dto.newName());
                return ResponseEntity.ok("Changement de nom effectué avec succès");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous ne possédé pas se personnage");
            }

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteCharacter(@PathVariable int id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try{
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            if (characterService.userOwnsCharacter(tokenUserId,id)){
                characterService.deleteCharacterById(id);
                return ResponseEntity.ok("Suppression effectuée avec succès");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Vous ne possédé pas se personnage");

            }
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppresion : " + e.getMessage());
        }

    }
}

