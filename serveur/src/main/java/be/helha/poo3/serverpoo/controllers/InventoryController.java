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
            return ResponseEntity.badRequest().build();
        }

        ObjectId objectId = new ObjectId(id);
        Inventory inventory = inventoryService.getInventory(objectId);

        if (inventory == null) {
            return ResponseEntity.notFound().build();
        }

        InventoryDTO dto = new InventoryDTO(inventory);
        System.out.println(dto);
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
     * Ajoute un nouvel item à l'inventaire donné à partir d'un item existant dans la base des modèles.
     *
     * @param inventoryId l'identifiant de l'inventaire ciblé
     * @param payload contient un champ "itemId" qui référence l'item à cloner
     * @return 200 OK si ajouté, 400 ou 404 avec message d'erreur sinon
     */
    @PostMapping("/{inventoryId}/items")
    public ResponseEntity<String> addItem(@PathVariable String inventoryId, @RequestBody Map<String, String> payload) {
        String rawItemId = payload.get("itemId");

        if (rawItemId == null || rawItemId.isBlank()) {
            return ResponseEntity.badRequest().body("{\"erreur\": \"Le champ 'itemId' est manquant ou vide dans la requête.\"}");
        }

        if (!ObjectId.isValid(rawItemId)) {
            return ResponseEntity.badRequest().body("{\"erreur\": \"L'identifiant fourni n'est pas un ObjectId valide.\"}");
        }

        ObjectId invId = new ObjectId(inventoryId);
        ObjectId itemId = new ObjectId(rawItemId);

        try {
            inventoryService.addItemToInventory(invId, itemId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("{\"erreur\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Supprime un item d’un inventaire à partir de son identifiant.
     *
     * @param inventoryId l'identifiant de l'inventaire
     * @param itemId l'identifiant de l'item à retirer
     * @return 200 OK si supprimé, 404 si l'item n'existe pas, ou 400 si les identifiants sont invalides
     */
    @DeleteMapping("/{inventoryId}/items/{itemId}")
    public ResponseEntity<String> removeItem(@PathVariable String inventoryId, @PathVariable String itemId) {
        if (!ObjectId.isValid(inventoryId)) {
            return ResponseEntity.badRequest().body("{\"erreur\": \"L'identifiant d'inventaire est invalide.\"}");
        }

        if (!ObjectId.isValid(itemId)) {
            return ResponseEntity.badRequest().body("{\"erreur\": \"L'identifiant fourni n'est pas un ObjectId valide.\"}");
        }

        ObjectId invId = new ObjectId(inventoryId);
        ObjectId itemObjId = new ObjectId(itemId);

        boolean removed = inventoryService.removeItemFromInventory(invId, itemObjId);
        if (!removed) {
            return ResponseEntity.status(404).body("{\"erreur\": \"Aucun item avec cet ID n’a été trouvé dans l’inventaire.\"}");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Consomme un item de l'inventaire donné en décrémentant son champ "currentCapacity" s'il est présent.
     * Supprime l'item s'il ne reste plus d'utilisation.
     *
     * @param inventoryId l'identifiant de l'inventaire
     * @param itemId l'identifiant de l'item à consommer
     * @return 200 si consommé ou supprimé, 404 si l'objet est introuvable, 400 en cas d'erreur
     */
    @PatchMapping("/{inventoryId}/items/{itemId}/consume")
    public ResponseEntity<String> consumeItem(
            @PathVariable String inventoryId,
            @PathVariable String itemId) {

        if (!ObjectId.isValid(inventoryId) || !ObjectId.isValid(itemId)) {
            return ResponseEntity.badRequest().body("{\"erreur\": \"ID invalide.\"}");
        }

        try {
            boolean success = inventoryService.consumeItem(
                    new ObjectId(inventoryId),
                    new ObjectId(itemId)
            );

            if (!success) {
                return ResponseEntity.status(404).body("{\"erreur\": \"Objet non trouvé dans l’inventaire.\"}");
            }

            return ResponseEntity.ok("{\"message\": \"Objet consommé.\"}");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"erreur\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Équipe un item dans un des emplacements de l'inventaire (main, armor, second).
     *
     * @param inventoryId l'identifiant de l'inventaire
     * @param slot le nom de l'emplacement ciblé (main, armor, second)
     * @param item l'objet à équiper, provenant de l'inventaire
     * @return 200 si l'équipement a réussi, 400 en cas d'erreur
     */
    @PostMapping("/{inventoryId}/equip/{slot}")
    public ResponseEntity<String> pushToSlot(
            @PathVariable String inventoryId,
            @PathVariable String slot,
            @RequestBody Item item) {

        if (!ObjectId.isValid(inventoryId)) {
            return ResponseEntity.badRequest().body("ID d'inventaire invalide.");
        }

        try {
            inventoryService.pushToSlot(new ObjectId(inventoryId), slot, item);
            return ResponseEntity.ok("Item équipé dans le slot " + slot);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Retire un item d’un emplacement de l’inventaire et le remet dans la liste d’objets si possible.
     * Si l’inventaire est plein et force est à true, l’item est supprimé.
     *
     * @param inventoryId l'identifiant de l'inventaire
     * @param slot le nom de l'emplacement ciblé (main, armor, second)
     * @param force true pour forcer la suppression si l'inventaire est plein
     * @return 200 si l'item a été retiré, 400 en cas d'erreur
     */
    @PostMapping("/{inventoryId}/unequip/{slot}")
    public ResponseEntity<String> pullFromSlot(
            @PathVariable String inventoryId,
            @PathVariable String slot,
            @RequestParam(defaultValue = "false") boolean force) {

        if (!ObjectId.isValid(inventoryId)) {
            return ResponseEntity.badRequest().body("ID d'inventaire invalide.");
        }

        try {
            inventoryService.pullFromSlot(new ObjectId(inventoryId), slot, force);
            return ResponseEntity.ok("Item retiré du slot " + slot);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }
}
