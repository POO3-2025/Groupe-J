package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.components.ConnexionMongoDB;
import be.helha.poo3.serverpoo.components.DynamicClassGenerator;
import be.helha.poo3.serverpoo.exceptions.InventoryIOException;
import be.helha.poo3.serverpoo.models.Inventory;
import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.utils.GsonObjectIdAdapted;
import com.google.gson.Gson;
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
/**
 * Service principal pour la gestion des inventaires dans le jeu.
 *
 * Cette classe assure toutes les opérations CRUD sur les inventaires MongoDB,
 * y compris la gestion des emplacements d'équipement (main, armor, second),
 * la consommation d'objets, l'ajout et la suppression d'items, ainsi que
 * l'adaptation dynamique des objets à leurs classes spécifiques via le système de génération.
 *
 * Le service repose sur MongoDB pour le stockage, Gson pour la (dé)sérialisation,
 * et des classes utilitaires pour le chargement dynamique d'objets.
 */
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

    /**
     * Crée un nouvel inventaire vide, le convertit en Document BSON
     * et l'insère dans la collection MongoDB "Inventories".
     *
     * @return l'objet Inventory nouvellement créé, avec son identifiant MongoDB (_id) défini.
     */
    public Inventory createInventory() {
        // Créer l'inventaire avec un ObjectId déjà initialisé
        Inventory inventory = new Inventory();

        // Sérialiser en JSON avec ObjectIdAdapter
        Gson gson = GsonObjectIdAdapted.getGson();
        String json = gson.toJson(inventory);
        Document doc = Document.parse(json);

        // Forcer le champ _id à rester un ObjectId (pas une String)
        doc.put("_id", inventory.getId());

        // S'assurer que le champ items est un tableau (pas un objet)
        if (!doc.containsKey("items") || !(doc.get("items") instanceof List)) {
            doc.put("items", new ArrayList<>());
        }

        // Insertion dans MongoDB
        inventoryCollection.insertOne(doc);

        // Retourner l'inventaire créé
        return inventory;
    }

    /**
     * Récupère un inventaire depuis la base MongoDB à partir de son identifiant.
     * Les items de l'inventaire ainsi que les slots d'équipement (main, armor, second)
     * sont reconstruits dynamiquement selon leur type pour restaurer leur classe spécifique.
     *
     * @param id l'identifiant MongoDB de l'inventaire à récupérer
     * @return un objet Inventory entièrement reconstruit ou null si aucun inventaire n'a été trouvé
     */
    public Inventory getInventory(ObjectId id) {
        Document doc = inventoryCollection.find(Filters.eq("_id", id)).first();
        if (doc == null) return null;

        Gson gson = GsonObjectIdAdapted.getGson();

        Inventory inventory = new Inventory();
        inventory.setId(id);

        // Reconstruction dynamique des items
        List<Item> realItems = new ArrayList<>();
        List<Document> itemDocs = (List<Document>) doc.get("items");

        if (itemDocs != null) {
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
        }

        inventory.setItems(realItems);

        // Reconstruction dynamique des slots
        inventory.setMainSlot(reconstructSlot((Document) doc.get("mainSlot"), gson));
        inventory.setArmorSlot(reconstructSlot((Document) doc.get("armorSlot"), gson));
        inventory.setSecondSlot(reconstructSlot((Document) doc.get("secondSlot"), gson));

        return inventory;
    }

    /**
     * Récupère la liste des items d'un inventaire à partir de son identifiant.
     * Si aucun inventaire n'est trouvé, retourne une liste vide.
     *
     * @param inventoryId l'identifiant MongoDB de l'inventaire
     * @return une liste d'objets Item contenus dans l'inventaire, ou une liste vide si l'inventaire est introuvable
     */
    public List<Item> getItems(ObjectId inventoryId) {
        Inventory inventory = getInventory(inventoryId);
        return inventory != null ? inventory.getItems() : new ArrayList<>();
    }

    /**
     * Ajoute un nouvel item à un inventaire spécifié en le clonant à partir d'un modèle existant
     * chargé par l'ItemLoaderService
     * L'item cloné reçoit un nouvel identifiant avant d'être inséré dans la base MongoDB.
     *
     * @param inventoryId l'identifiant MongoDB de l'inventaire cible
     * @param itemId l'identifiant de l'item modèle à cloner et ajouter
     * @return l'objet {@code Item} cloné
     * @throws RuntimeException si aucun item avec l'identifiant donné n'est trouvé dans les modèles chargés
     */
    public Item addItemToInventory(ObjectId inventoryId, ObjectId itemId) {
        // Trouver l’item d’origine via l’ItemLoaderService
        Item original = itemLoaderService.getLoadedItems().stream()
                .filter(i -> itemId.equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun item trouvé avec l'identifiant : " + itemId));

        // Cloner dynamiquement
        Item clone = cloneItem(original);

        // Générer un nouvel ID pour l’item cloné
        clone.setId(new ObjectId());

        // Convertir en Document pour MongoDB
        Document itemDoc = Document.parse(gson.toJson(clone));

        // Ajouter dans l’inventaire
        inventoryCollection.updateOne(
                Filters.eq("_id", inventoryId),
                Updates.push("items", itemDoc)
        );
        return clone;
    }

    /**
     * Supprime un item spécifique d'un inventaire MongoDB, en le recherchant par son identifiant.
     * L'inventaire est récupéré depuis la base, et ses items sont désérialisés dynamiquement
     * selon leur type à l'aide du système de classes générées. Si l'item est trouvé dans la liste,
     * il est supprimé, et l'inventaire mis à jour dans la base de données.
     *
     * Aucun mapping complet de l'objet Inventory n'est effectué, seule la liste des items est manipulée.
     *
     * @param inventoryId l'identifiant MongoDB de l'inventaire
     * @param itemId l'identifiant de l'item à supprimer
     * @return true si l'item a été trouvé et supprimé, false sinon
     */
    public boolean removeItemFromInventory(ObjectId inventoryId, ObjectId itemId) {
        // Récupérer le document de l'inventaire
        Document inventoryDoc = inventoryCollection.find(Filters.eq("_id", inventoryId)).first();
        if (inventoryDoc == null) return false;

        Gson gson = GsonObjectIdAdapted.getGson();

        // Désérialiser correctement les items dynamiques
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

        // Supprimer un seul item par son _id
        boolean removed = realItems.removeIf(item -> itemId.equals(item.getId()));

        if (!removed) return false;

        // Convertir les items restants en Documents
        List<Document> updatedItemDocs = new ArrayList<>();
        for (Item item : realItems) {
            updatedItemDocs.add(Document.parse(gson.toJson(item)));
        }

        // MàJ dans la base
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
            // Nouvelle instance de la même classe
            Class<?> clazz = original.getClass();
            Item copy = (Item) clazz.getDeclaredConstructor().newInstance();

            // Copier les champs déclarés dans Item
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

    /**
     * Consomme un item dans un inventaire MongoDB, identifié par son ObjectId.
     *
     * L'item ciblé doit contenir un champ dynamique nommé "currentCapacity" :
     *
     *   - Si sa valeur est supérieure à 1, elle est décrémentée de 1
     *   - Si elle vaut 1 ou moins, l'item est retiré de l'inventaire
     *
     * Les items sont reconstruits dynamiquement à partir de leur type avant traitement.
     * L'inventaire est mis à jour en base si l'opération a modifié la liste.
     *
     * @param inventoryId l'identifiant MongoDB de l'inventaire
     * @param itemId l'identifiant de l'item à consommer
     * @return true si l'item a été consommé ou supprimé, false sinon (ex. item introuvable)
     * @throws RuntimeException si le champ "currentCapacity" est manquant ou inaccessible
     */
    public boolean consumeItem(ObjectId inventoryId, ObjectId itemId) {
        Document inventoryDoc = inventoryCollection.find(Filters.eq("_id", inventoryId)).first();
        if (inventoryDoc == null) return false;

        Gson gson = GsonObjectIdAdapted.getGson();

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

    /**
     * Équipe un item dans un des emplacements d'équipement (main, armor ou second)
     * de l'inventaire spécifié.
     *
     * L'item doit être présent dans la liste des objets de l'inventaire (par son ID).
     * Cette méthode effectue une vérification de type et de présence avant d’équiper.
     * Si un autre item est déjà équipé à cet emplacement, il sera replacé dans l’inventaire.
     *
     * @param inventoryId l'identifiant MongoDB de l'inventaire
     * @param slot le nom de l’emplacement ciblé (main, armor ou second)
     * @param item un objet contenant uniquement l'ID de l'item à équiper
     * @throws InventoryIOException si l'inventaire est introuvable, l'item absent ou le slot invalide
     */
    public void pushToSlot(ObjectId inventoryId, String slot, Item item) throws InventoryIOException {
        Inventory inv = getInventory(inventoryId);
        if (inv == null) throw new InventoryIOException("Inventaire introuvable", 2);

        // Rechercher l’item réel dans l’inventaire (sécurité si le JSON est partiel)
        Item realItem = inv.getItems().stream()
                .filter(i -> i.getId().equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new InventoryIOException("L'item n'a pas été trouvé dans l'inventaire.", 2));

        // Équiper selon le slot
        switch (slot.toLowerCase()) {
            case "main" -> inv.pushToMainSlot(realItem.getId());
            case "armor" -> inv.pushToArmorSlot(realItem.getId());
            case "second" -> inv.pushToSecondSlot(realItem.getId());
            default -> throw new InventoryIOException("Slot invalide", 99);
        }

        updateInventory(inv);
    }

    /**
     * Retire l'item d'un des emplacements d'équipement (main, armor, second) pour le replacer dans l'inventaire.
     * Si l'inventaire est plein, le paramètre force détermine si l'item doit être supprimé.
     *
     * @param inventoryId l'identifiant de l'inventaire
     * @param slot le nom de l'emplacement ciblé (main, armor ou second)
     * @param force si vrai, l'item est supprimé en cas d'impossibilité de le replacer dans l'inventaire
     * @throws InventoryIOException si l'inventaire est introuvable ou le slot invalide
     */
    public void pullFromSlot(ObjectId inventoryId, String slot, boolean force) throws InventoryIOException {
        Inventory inv = getInventory(inventoryId);
        if (inv == null) throw new InventoryIOException("Inventaire introuvable", 2);

        if (slot == null) throw new InventoryIOException("Aucun slot spécifié", 99);

        switch (slot.toLowerCase()) {
            case "main" -> inv.pullFromMainSlot(force);
            case "armor" -> inv.pullFromArmorSlot(force);
            case "second" -> inv.pullFromSecondSlot(force);
            default -> throw new InventoryIOException("Slot invalide", 99);
        }

        updateInventory(inv);
    }

    /**
     * Met à jour un document d'inventaire dans la base de données MongoDB
     * en remplaçant l'entrée existante par une nouvelle version sérialisée.
     *
     * Le champ "_id" est explicitement réinjecté au bon format pour éviter les conflits
     * liés à la conversion Gson (qui peut le transformer en String).
     *
     * @param inv l'objet inventaire à sauvegarder dans la base
     */
    private void updateInventory(Inventory inv) {
        // Sérialisation en JSON
        String json = gson.toJson(inv);
        Document doc = Document.parse(json);

        // Forcer à nouveau le champ _id à être un ObjectId (évite l'erreur Mongo code 66)
        doc.put("_id", inv.getId());

        // Remplacement en base (équivalent PUT)
        inventoryCollection.replaceOne(
                Filters.eq("_id", inv.getId()),
                doc
        );
    }

    /**
     * Reconstruit dynamiquement un objet item à partir d'un document BSON représentant un slot d'équipement.
     * Utilise le champ "type" pour déterminer la classe spécifique à instancier.
     *
     * @param doc le document BSON représentant un slot (main, armor ou second)
     * @param gson l'instance Gson utilisée pour la désérialisation
     * @return un objet de type Item reconstruit dynamiquement ou en tant qu'Item générique si le type est inconnu
     */
    private Item reconstructSlot(Document doc, Gson gson) {
        if (doc == null) return null;

        String type = doc.getString("type");
        Class<?> clazz = DynamicClassGenerator.getClassByName(type);

        if (clazz != null) {
            Object obj = gson.fromJson(doc.toJson(), clazz);
            if (obj instanceof Item item) {
                return item;
            }
        }

        return gson.fromJson(doc.toJson(), Item.class);
    }
}
