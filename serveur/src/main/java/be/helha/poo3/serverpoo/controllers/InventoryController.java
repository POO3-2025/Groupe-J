package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Inventory;
import be.helha.poo3.serverpoo.models.InventoryDTO;
import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.services.InventoryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Crée un nouvel inventaire vide et retourne son identifiant.
     */
    @PostMapping
    public ResponseEntity<Inventory> createInventory() {
        Inventory created = inventoryService.createInventory();
        return ResponseEntity.ok(created);
    }

    /**
     * Récupère un inventaire par son ID MongoDB.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getInventory(@PathVariable String id) {
        if (!ObjectId.isValid(id)) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        ObjectId objectId = new ObjectId(id);
        Inventory inventory = inventoryService.getInventory(objectId);

        if (inventory == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        InventoryDTO dto = new InventoryDTO(inventory);
        return ResponseEntity.ok(dto);
    }

    /**
     * Retourne les objets contenus dans un inventaire.
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<List<Item>> getItems(@PathVariable String id) {
        List<Item> items = inventoryService.getItems(new ObjectId(id));
        return ResponseEntity.ok(items);
    }

    /**
     * Ajoute un nouvel item à l'inventaire donné.
     */
    @PostMapping("/{inventoryId}/items")
    public ResponseEntity<String> addItem(@PathVariable String inventoryId, @RequestBody Map<String, String> payload) {
        String rawItemId = payload.get("itemId");

        if (rawItemId == null || rawItemId.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body("{\"erreur\": \"Le champ 'itemId' est manquant ou vide dans la requête.\"}");
        }

        if (!ObjectId.isValid(rawItemId)) {
            return ResponseEntity
                    .badRequest()
                    .body("{\"erreur\": \"L'identifiant fourni n'est pas un ObjectId valide.\"}");
        }

        ObjectId invId = new ObjectId(inventoryId);
        ObjectId itemId = new ObjectId(rawItemId);

        try {
            inventoryService.addItemToInventory(invId, itemId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(404)
                    .body("{\"erreur\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Supprime un item de l'inventaire par son identifiant.
     */
    @DeleteMapping("/{inventoryId}/items/{itemId}")
    public ResponseEntity<String> removeItem(@PathVariable String inventoryId, @PathVariable String itemId) {
        if (!ObjectId.isValid(inventoryId)) {
            return ResponseEntity
                    .badRequest()
                    .body("{\"erreur\": \"L'identifiant d'inventaire est invalide.\"}");
        }

        if (!ObjectId.isValid(itemId)) {
            return ResponseEntity
                    .badRequest()
                    .body("{\"erreur\": \"L'identifiant fourni n'est pas un ObjectId valide.\"}");
        }

        ObjectId invId = new ObjectId(inventoryId);
        ObjectId itemObjId = new ObjectId(itemId);

        boolean removed = inventoryService.removeItemFromInventory(invId, itemObjId);
        if (!removed) {
            return ResponseEntity
                    .status(404)
                    .body("{\"erreur\": \"Aucun item avec cet ID n’a été trouvé dans l’inventaire.\"}");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Consomme un item de l'inventaire donné.
     * Si l'item est un consommable (possède un champ 'currentCapacity'), décrémente sa capacité.
     * Supprime automatiquement l'item s'il ne reste plus d'utilisations.
     *
     * @param inventoryId l'identifiant de l'inventaire
     * @param itemId l'identifiant de l'item à consommer
     * @return true si l'item a été consommé ou supprimé, false s'il n'a pas été trouvé
     *
     * @throws RuntimeException si l'item n'est pas un consommable
     *
     * /!\ Méthode générée et adaptée par une IA (ChatGPT)
     */

    @PatchMapping("/{inventoryId}/items/{itemId}/consume")
    public ResponseEntity<String> consumeItem(
            @PathVariable String inventoryId,
            @PathVariable String itemId) {

        if (!ObjectId.isValid(inventoryId) || !ObjectId.isValid(itemId)) {
            return ResponseEntity
                    .badRequest()
                    .body("{\"erreur\": \"ID invalide.\"}");
        }

        try {
            boolean success = inventoryService.consumeItem(
                    new ObjectId(inventoryId),
                    new ObjectId(itemId)
            );

            if (!success) {
                return ResponseEntity
                        .status(404)
                        .body("{\"erreur\": \"Objet non trouvé dans l’inventaire.\"}");
            }

            return ResponseEntity.ok("{\"message\": \"Objet consommé.\"}");

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body("{\"erreur\": \"" + e.getMessage() + "\"}");
        }
    }
}