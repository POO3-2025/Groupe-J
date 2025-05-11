package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.components.ConnexionMongoDB;
import be.helha.poo3.serverpoo.components.DynamicClassGenerator;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Primary
@Service
public class InventoryService {

    private final ConnexionMongoDB connexionMongoDB;
    private final MongoCollection<Document> inventoryCollection;
    private final Gson gson;
    private final ItemLoaderService itemLoaderService;
    private final DynamicClassGenerator dynamicClassGenerator;

    public InventoryService(ConnexionMongoDB connexionMongoDB, ItemLoaderService itemLoaderService, DynamicClassGenerator dynamicClassGenerator) {
        this.connexionMongoDB = connexionMongoDB;
        this.inventoryCollection = connexionMongoDB.getCollection("Inventories");
        this.gson = GsonObjectIdAdapted.getGson();
        this.itemLoaderService = itemLoaderService;
        this.dynamicClassGenerator = dynamicClassGenerator;
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
        if (doc == null) return null;

        Gson gson = GsonObjectIdAdapted.getGson();
        Inventory inventory = gson.fromJson(doc.toJson(), Inventory.class);

        // reconstruire dynamiquement chaque item
        List<Item> realItems = new ArrayList<>();
        List<Document> itemDocs = (List<Document>) doc.get("items");

        for (Document itemDoc : itemDocs) {
            String type = itemDoc.getString("type");
            Class<?> clazz = DynamicClassGenerator.getClassByName(type);

            if (clazz != null) {
                Object itemObj = gson.fromJson(itemDoc.toJson(), clazz);
                if (itemObj instanceof Item item) {
                    realItems.add(item);
                }
            } else {
                realItems.add(gson.fromJson(itemDoc.toJson(), Item.class));
            }
        }

        inventory.setItems(realItems);
        return inventory;
    }


    public List<Item> getItems(ObjectId inventoryId) {
        Inventory inventory = getInventory(inventoryId);
        return inventory != null ? inventory.getItems() : new ArrayList<>();
    }

    public void addItemToInventory(ObjectId inventoryId, ObjectId itemId) {
        // trouver l’item d’origine via l’ItemLoaderService
        Item original = itemLoaderService.getLoadedItems().stream()
                .filter(i -> itemId.equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun item trouvé avec l'identifiant : " + itemId));

        // cloner dynamiquement
        Item clone = cloneItem(original);

        // générer un nouvel ID pour l’item cloné
        clone.setId(new ObjectId());

        // convertir en Document pour MongoDB
        Document itemDoc = Document.parse(gson.toJson(clone));

        // ajouter dans l’inventaire
        inventoryCollection.updateOne(
                Filters.eq("_id", inventoryId),
                Updates.push("items", itemDoc)
        );
    }

    public boolean removeItemFromInventory(ObjectId inventoryId, ObjectId itemId) {
        // récupérer le document de l'inventaire
        Document inventoryDoc = inventoryCollection.find(Filters.eq("_id", inventoryId)).first();
        if (inventoryDoc == null) return false;

        Gson gson = GsonObjectIdAdapted.getGson();
        Inventory inventory = gson.fromJson(inventoryDoc.toJson(), Inventory.class);

        // désérialiser correctement les items dynamiques
        List<Item> realItems = new ArrayList<>();
        List<Document> itemDocs = (List<Document>) inventoryDoc.get("items");

        for (Document itemDoc : itemDocs) {
            String type = itemDoc.getString("type");
            Class<?> clazz = DynamicClassGenerator.getClassByName(type);

            Item item = clazz != null
                    ? (Item) gson.fromJson(itemDoc.toJson(), clazz)
                    : gson.fromJson(itemDoc.toJson(), Item.class);

            realItems.add(item);
        }

        // supprimer un seul item par son _id
        boolean removed = realItems.removeIf(item -> itemId.equals(item.getId()));

        if (!removed) return false;

        // convertir les items restants en Documents
        List<Document> updatedItemDocs = new ArrayList<>();
        for (Item item : realItems) {
            updatedItemDocs.add(Document.parse(gson.toJson(item)));
        }

        // màj dans la base
        inventoryCollection.updateOne(
                Filters.eq("_id", inventoryId),
                Updates.set("items", updatedItemDocs)
        );

        return true;
    }


    /**
     * Crée un clone indépendant de l'Item passé en paramètre.
     * Compatible avec toutes les classes d'items générées dynamiquement.
     *
     * @param original l'instance d'origine à cloner
     * @return une nouvelle instance de la même classe, avec les mêmes valeurs
     */
    public static Item cloneItem(Item original) {
        if (original == null) return null;

        try {
            // nouvelle instance de la même classe
            Class<?> clazz = original.getClass();
            Item copy = (Item) clazz.getDeclaredConstructor().newInstance();

            // copier les champs déclarés dans Item
            copy.setId(original.getId());
            copy.setName(original.getName());
            copy.setType(original.getType());
            copy.setSubType(original.getSubType());
            copy.setRarity(original.getRarity());
            copy.setDescription(original.getDescription());

            for (String attr : original.getAdditionalAttributes()) {
                int val = original.getInt(attr);
                copy.setInt(attr, val);
            }

            return copy;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de cloner l'item: " + e.getMessage(), e);
        }
    }

    public boolean consumeItem(ObjectId inventoryId, ObjectId itemId) {
        Document inventoryDoc = inventoryCollection.find(Filters.eq("_id", inventoryId)).first();
        if (inventoryDoc == null) return false;

        Gson gson = GsonObjectIdAdapted.getGson();
        Inventory inventory = gson.fromJson(inventoryDoc.toJson(), Inventory.class);

        List<Document> itemDocs = (List<Document>) inventoryDoc.get("items");
        List<Item> realItems = new ArrayList<>();

        for (Document itemDoc : itemDocs) {
            String type = itemDoc.getString("type");
            Class<?> clazz = DynamicClassGenerator.getClassByName(type);
            Item item = clazz != null
                    ? (Item) gson.fromJson(itemDoc.toJson(), clazz)
                    : gson.fromJson(itemDoc.toJson(), Item.class);
            realItems.add(item);
        }

        boolean updated = false;
        Iterator<Item> iterator = realItems.iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (itemId.equals(item.getId())) {
                try {
                    Field field = item.getClass().getDeclaredField("currentCapacity");
                    field.setAccessible(true);

                    int value = (int) field.get(item);

                    if (value > 1) {
                        field.set(item, value - 1);
                    } else {
                        iterator.remove();
                    }

                    updated = true;
                    break;

                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Cet objet n’est pas consommable (champ currentCapacity absent).");
                } catch (Exception e) {
                    throw new RuntimeException("Erreur lors de la consommation de l’objet", e);
                }
            }
        }

        if (!updated) return false;

        List<Document> updatedDocs = new ArrayList<>();
        for (Item item : realItems) {
            updatedDocs.add(Document.parse(gson.toJson(item)));
        }

        inventoryCollection.updateOne(
                Filters.eq("_id", inventoryId),
                Updates.set("items", updatedDocs)
        );

        return true;
    }
}
