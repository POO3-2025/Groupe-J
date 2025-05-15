package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.*;
import be.helha.poo3.serverpoo.services.InGameCharacterService;
import be.helha.poo3.serverpoo.services.InventoryService;
import be.helha.poo3.serverpoo.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour gérer les inventaires.
 * Toutes les actions CRUD sur les objets contenus dans un coffre MongoDB passent par ce contrôleur.
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InGameCharacterService inGameCharacterService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Crée un nouvel inventaire vide et retourne son identifiant.
     */
    @PostMapping
    public ResponseEntity<Inventory> createInventory() {
        Inventory created = inventoryService.createInventory();
        return ResponseEntity.ok(created);
    }

    /**
     * Récupère l'inventaire du personnage actuellement en jeu de l'utilisateur authentifié.
     * Le token JWT est extrait de l'en-tête Authorization pour identifier l'utilisateur.
     *
     * Étapes :
     * - Vérifie la présence du token
     * - Extrait l'ID utilisateur depuis le JWT
     * - Récupère le personnage actif associé à l'utilisateur
     * - Charge l'inventaire MongoDB lié au personnage
     *
     * @param authHeader l'en-tête HTTP Authorization contenant le token JWT
     * @return un objet InventoryDTO si tout est valide ; sinon, un code d'erreur HTTP :
     *         - 403 si aucun personnage actif ou header manquant
     *         - 401 si le token est invalide
     *         - 404 si l'inventaire n'existe pas
     *         - 500 si une autre erreur se produit
     */
    @GetMapping
    public ResponseEntity<?> getInventory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("L'en-tête Authorization est manquant.");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            int userId = jwtUtils.getUserIdFromToken(token);

            // Récupérer le perso actif en jeu
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(userId);

            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Aucun personnage actif pour cet utilisateur.");
            }

            // Récupérer l'inventaire
            ObjectId inventoryId = new ObjectId(character.getInventoryId());
            //System.out.println(inventoryId); //debug
            Inventory inventory = inventoryService.getInventory(inventoryId);

            if (inventory == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventaire introuvable.");
            }

            return ResponseEntity.ok(new InventoryDTO(inventory));

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération de l'inventaire : " + e.getMessage());
        }
    }

    /**
     * Retourne les objets contenus dans l'inventaire du personnage actuellement en jeu.
     * Authentification via token JWT obligatoire.
     */
    @GetMapping("/items")
    public ResponseEntity<?> getItems(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("L'en-tête Authorization est manquant.");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            int userId = jwtUtils.getUserIdFromToken(token);

            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(userId);
            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Aucun personnage actif pour cet utilisateur.");
            }

            ObjectId inventoryId = new ObjectId(character.getInventoryId());
            List<Item> items = inventoryService.getItems(inventoryId);

            return ResponseEntity.ok(items);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération des items : " + e.getMessage());
        }
    }

    /**
     * Ajoute un nouvel item à l'inventaire du personnage en jeu de l'utilisateur authentifié.
     *
     * @param payload contient un champ "itemId" qui référence l'item à cloner
     * @param authHeader l'en-tête HTTP Authorization contenant le token JWT
     * @return 200 OK si l'item a été ajouté avec succès, 400 ou 404 avec message d'erreur sinon
     */
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addItem(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> payload) {

        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erreur", "L'en-tête Authorization est manquant."));
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        String rawItemId = payload.get("itemId");

        if (rawItemId == null || rawItemId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Le champ 'itemId' est manquant ou vide dans la requête."));
        }

        if (!ObjectId.isValid(rawItemId)) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "L'identifiant fourni n'est pas un ObjectId valide."));
        }

        ObjectId itemId = new ObjectId(rawItemId);

        try {
            int userId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(userId);

            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erreur", "Aucun personnage actif pour cet utilisateur."));
            }

            ObjectId inventoryId = new ObjectId(character.getInventoryId());
            inventoryService.addItemToInventory(inventoryId, itemId);

            Inventory updated = inventoryService.getInventory(inventoryId);
            Item last = updated.getItems().isEmpty() ? null : updated.getItems().get(updated.getItems().size() - 1);

            if (last == null) {
                return ResponseEntity.status(500).body(Map.of("erreur", "L'ajout a échoué, item introuvable."));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Item ajouté à l'inventaire.",
                    "item", last.getMap()
            ));

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erreur", "Token invalide : " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("erreur", "Erreur : " + e.getMessage()));
        }
    }

    /**
     * Supprime un item de l'inventaire du personnage actuellement en jeu.
     *
     * @param itemId l'identifiant de l'item à retirer
     * @param authHeader l'en-tête HTTP Authorization contenant le token JWT
     * @return 200 OK si supprimé, 404 si l'item n'existe pas, ou 400 si l'identifiant est invalide
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<String> removeItem(
            @PathVariable String itemId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("L'en-tête Authorization est manquant.");
        }

        if (!ObjectId.isValid(itemId)) {
            return ResponseEntity.badRequest().body("L'identifiant de l'item est invalide.");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            int userId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(userId);

            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Aucun personnage actif pour cet utilisateur.");
            }

            ObjectId inventoryId = new ObjectId(character.getInventoryId());
            ObjectId itemObjId = new ObjectId(itemId);

            boolean removed = inventoryService.removeItemFromInventory(inventoryId, itemObjId);
            if (!removed) {
                return ResponseEntity.status(404).body("Aucun item avec cet ID n’a été trouvé dans l’inventaire.");
            }

            return ResponseEntity.ok("Item supprimé.");

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Consomme un item dans l'inventaire du personnage actuellement en jeu.
     * Si l'item possède un champ "currentCapacity", sa valeur est décrémentée.
     * L'item est automatiquement supprimé s'il ne reste plus d'utilisations.
     *
     * @param itemId l'identifiant de l'item à consommer
     * @param authHeader le token JWT dans l'en-tête Authorization
     * @return 200 si consommé ou supprimé, 404 si introuvable, 401 si token invalide, 400 en cas d'erreur
     */
    @PatchMapping("/items/{itemId}/consume")
    public ResponseEntity<String> consumeItem(
            @PathVariable String itemId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("L'en-tête Authorization est manquant.");
        }

        if (!ObjectId.isValid(itemId)) {
            return ResponseEntity.badRequest().body("Identifiant d'item invalide.");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            int userId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(userId);

            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Aucun personnage actif pour cet utilisateur.");
            }

            ObjectId inventoryId = new ObjectId(character.getInventoryId());
            ObjectId itemObjId = new ObjectId(itemId);

            boolean success = inventoryService.consumeItem(inventoryId, itemObjId);

            if (!success) {
                return ResponseEntity.status(404).body("Objet non trouvé dans l’inventaire.");
            }

            return ResponseEntity.ok("Objet consommé.");

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    /**
     * Équipe un item dans un des emplacements (main, armor ou second) de l'inventaire
     * du personnage actuellement en jeu de l'utilisateur authentifié.
     *
     * L'item doit déjà être présent dans l'inventaire MongoDB. Si le slot est occupé,
     * l'ancien item est replacé dans la liste d'objets.
     *
     * @param slot le nom du slot d’équipement ciblé
     * @param item l'objet à équiper
     * @param authHeader l'en-tête HTTP Authorization contenant le token JWT
     * @return 200 si l’équipement a réussi, 400 ou 401 en cas d’erreur
     */
    @PostMapping("/equip/{slot}")
    public ResponseEntity<String> pushToSlot(
            @PathVariable String slot,
            @RequestBody Item item,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("L'en-tête Authorization est manquant.");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            int userId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(userId);

            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Aucun personnage actif pour cet utilisateur.");
            }

            ObjectId inventoryId = new ObjectId(character.getInventoryId());

            inventoryService.pushToSlot(inventoryId, slot, item);
            return ResponseEntity.ok("Item équipé dans le slot " + slot);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    /**
     * Retire un item d’un emplacement de l’inventaire du personnage actuellement en jeu,
     * et le replace dans la liste d’objets si possible.
     * Si l’inventaire est plein et {@code force} est à true, l’item est supprimé.
     *
     * @param slot le nom de l'emplacement ciblé (main, armor, second)
     * @param force true pour forcer la suppression si l'inventaire est plein
     * @param authHeader le token JWT dans l'en-tête Authorization
     * @return 200 si l'item a été retiré, 400 ou 401 en cas d'erreur
     */
    @PostMapping("/unequip/{slot}")
    public ResponseEntity<String> pullFromSlot(
            @PathVariable String slot,
            @RequestParam(defaultValue = "false") boolean force,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("L'en-tête Authorization est manquant.");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            int userId = jwtUtils.getUserIdFromToken(token);
            CharacterWithPos character = inGameCharacterService.getInGameCharacterByUserId(userId);

            if (character == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Aucun personnage actif pour cet utilisateur.");
            }

            ObjectId inventoryId = new ObjectId(character.getInventoryId());

            inventoryService.pullFromSlot(inventoryId, slot, force);
            return ResponseEntity.ok("Item retiré du slot " + slot);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    /*
    /**
     * Supprime un inventaire par son identifiant.
     * L'utilisateur doit être authentifié via JWT.
     *
     * @param id l'identifiant MongoDB de l'inventaire à supprimer
     * @param authHeader le token JWT dans l'en-tête Authorization
     * @return 200 si supprimé, 404 si introuvable, 401 ou 403 selon le cas
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInventory(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("L'en-tête Authorization est manquant.");
        }

        if (!ObjectId.isValid(id)) {
            return ResponseEntity.badRequest().body("L'identifiant de l'inventaire est invalide.");
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            jwtUtils.getUserIdFromToken(token); // vérification uniquement

            ObjectId objectId = new ObjectId(id);
            boolean deleted = inventoryService.deleteInventory(objectId);

            if (!deleted) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Inventaire introuvable ou déjà supprimé.");
            }

            return ResponseEntity.ok("Inventaire supprimé.");

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur : " + e.getMessage());
        }
    }
    */
}
