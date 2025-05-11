package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.components.ConnexionMongoDB;
import be.helha.poo3.serverpoo.models.Inventory;
import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.utils.GsonObjectIdAdapted;
import be.helha.poo3.serverpoo.utils.ObjectIdAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Primary
@Service
public class InventoryService {

    private final ConnexionMongoDB connexionMongoDB;
    private final MongoCollection<Document> inventoryCollection;
    private final Gson gson;
    private final ItemLoaderService itemLoaderService;

    public InventoryService(ConnexionMongoDB connexionMongoDB, ItemLoaderService itemLoaderService) {
        this.connexionMongoDB = connexionMongoDB;
        this.inventoryCollection = connexionMongoDB.getCollection("Inventories");
        this.gson = GsonObjectIdAdapted.getGson();
        this.itemLoaderService = itemLoaderService;
    }

    public Inventory createInventory() {
        Inventory inventory = new Inventory();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())
                .create();

        String json = gson.toJson(inventory); // Inventory -> JSON string
        Document doc = Document.parse(json); // JSON string -> Document
        inventoryCollection.insertOne(doc);

        inventory.setId(doc.getObjectId("_id"));
        return inventory;
    }

    public Inventory getInventory(ObjectId id) {
        Document doc = inventoryCollection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())
                    .create();
            return gson.fromJson(doc.toJson(), Inventory.class);  // conversion JSON -> Inventory
        }
        return null;
    }

    public List<Item> getItems(ObjectId inventoryId) {
        Inventory inventory = getInventory(inventoryId);
        return inventory != null ? inventory.getItems() : new ArrayList<>();
    }

    public void addItemToInventory(ObjectId inventoryId, ObjectId itemId) {
        // Chercher l'item chargé en mémoire
        Item originalItem = itemLoaderService.getLoadedItems().stream()
                .filter(i -> itemId.equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun item trouvé avec l'identifiant : " + itemId));

        // Cloner l'item en JSON puis le désérialiser
        String json = gson.toJson(originalItem);
        Item clonedItem = gson.fromJson(json, originalItem.getClass());

        // Générer un nouvel ID
        clonedItem.setId(new ObjectId());

        // Ajouter dans Mongo
        Document itemDoc = Document.parse(gson.toJson(clonedItem));

        inventoryCollection.updateOne(
                Filters.eq("_id", inventoryId),
                Updates.push("items", itemDoc)
        );
    }

    public boolean removeItemFromInventory(ObjectId inventoryId, ObjectId itemId) {
        // Récupérer l'inventaire
        Document inventoryDoc = inventoryCollection.find(Filters.eq("_id", inventoryId)).first();
        if (inventoryDoc == null) return false;

        Inventory inventory = gson.fromJson(inventoryDoc.toJson(), Inventory.class);
        List<Item> items = inventory.getItems();

        if (items == null || items.isEmpty()) return false;

        // Supprimer le premier item ayant l’ID demandé
        boolean removed = false;
        for (int i = 0; i < items.size(); i++) {
            if (itemId.equals(items.get(i).getId())) {
                items.remove(i);
                removed = true;
                break;
            }
        }

        if (!removed) return false;

        // Reconstruire la liste en Documents
        List<Document> updatedItems = new ArrayList<>();
        for (Item item : items) {
            updatedItems.add(Document.parse(gson.toJson(item)));
        }

        // Mise à jour de la base
        inventoryCollection.updateOne(
                Filters.eq("_id", inventoryId),
                Updates.set("items", updatedItems)
        );

        return true;
    }
}
