package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.*;
import be.helha.poo3.serverpoo.services.InGameCharacterService;
import be.helha.poo3.serverpoo.services.PVPFightService;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fight/pvp")
public class PVPController {

    @Autowired
    private InGameCharacterService inGameCharacterService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PVPFightService fightService;

    @PostMapping(path = "/challenge/{targetId}")
    public ResponseEntity<?> challenge(@RequestHeader(value = "Authorization", required = false) String authHeader,
    @PathVariable int targetId ) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            return ResponseEntity.ok(new ChallengeRequestDTO(fightService.createChallenge(character.getIdCharacter(), targetId)));


        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping(path = "/challenge/{challengeId}/accept")
    public ResponseEntity<?> accept(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @PathVariable String challengeId ) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            return ResponseEntity.ok(new ChallengeRequestDTO(fightService.acceptChallenge(new ObjectId(challengeId), character.getIdCharacter())));


        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping(path = "/challenge/{challengeId}/decline")
    public ResponseEntity<?> decline(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @PathVariable String challengeId ) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            fightService.cancelChallenge(new ObjectId(challengeId), character.getIdCharacter());
            return ResponseEntity.ok().build();

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }

    }

    @GetMapping(path = "/challenge/toMe")
    public ResponseEntity<?> toMe(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            return ResponseEntity.ok(new ChallengeRequestDTO(fightService.getChallengeToMe(character.getIdCharacter())));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @GetMapping(path = "/challenge/fromMe")
    public ResponseEntity<?> fromMe(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            return ResponseEntity.ok(new ChallengeRequestDTO(fightService.getChallengeFromMe(character.getIdCharacter())));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @PostMapping(path = "/{action}")
    public ResponseEntity<?> action(@PathVariable String action, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            return ResponseEntity.ok(fightService.submitAction(character.getIdCharacter(), action));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing");
        }
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            int tokenUserId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(tokenUserId);
            PVPFight fight = fightService.getFightByPlayerId(character.getIdCharacter());
            if (fight == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Fight not found");
            return ResponseEntity.ok(fight.getTurns().get(fight.getTurns().size() - 1).getResult());
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }




}
