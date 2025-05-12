package be.helha.poo3.serverpoo.models;

import be.helha.poo3.serverpoo.exceptions.InventoryIOException;
import be.helha.poo3.serverpoo.utils.AllowedItemTypeUtil;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private ObjectId id;
    private List<Item> items;

    private Item armorSlot;
    private Item mainSlot;
    private Item secondSlot;

    public Inventory() {
        this.id = new ObjectId();
        this.items = new ArrayList<>();
    }

    public Inventory(ObjectId id, List<Item> items) {
        this.id = id;
        this.items = items != null ? items : new ArrayList<>();
        this.mainSlot = null;
        this.armorSlot = null;
        this.secondSlot = null;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(Item item) throws InventoryIOException {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        if (this.items.size() >= 10) {
            throw new InventoryIOException("L'inventaire est plein.", 1);
        }
        this.items.add(item);
    }

    public void removeItem(Item item) throws InventoryIOException {
        if (item == null || item.getId() == null) {
            throw new InventoryIOException("Item invalide.", 2);
        }

        boolean removed = false;

        // Suppression dans la liste d'items
        if (items != null) {
            removed = items.removeIf(i -> i.getId().equals(item.getId()));
        }

        // Suppression dans les slots d'équipement si nécessaire
        if (!removed) {
            if (mainSlot != null && mainSlot.getId().equals(item.getId())) {
                mainSlot = null;
                removed = true;
            } else if (armorSlot != null && armorSlot.getId().equals(item.getId())) {
                armorSlot = null;
                removed = true;
            } else if (secondSlot != null && secondSlot.getId().equals(item.getId())) {
                secondSlot = null;
                removed = true;
            }
        }

        if (!removed) {
            throw new InventoryIOException("L'item n'a pas été trouvé dans l'inventaire ou l'équipement.", 2);
        }
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + id +
                ", items=" + items +
                ", armorSlot=" + armorSlot +
                ", mainSlot=" + mainSlot +
                ", secondSlot=" + secondSlot +
                '}';
    }

    public Item getMainSlot() {
        return mainSlot;
    }

    public Item getArmorSlot() {
        return armorSlot;
    }

    public Item getSecondSlot() {
        return secondSlot;
    }

    public void setMainSlot(Item mainSlot) {
        this.mainSlot = mainSlot;
    }

    public void setArmorSlot(Item armorSlot) {
        this.armorSlot = armorSlot;
    }

    public void setSecondSlot(Item secondSlot) {
        this.secondSlot = secondSlot;
    }

    // Gestion des emplacements d'équipement

    public void pushToMainSlot(ObjectId itemId) throws InventoryIOException {
        Item foundItem = items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new InventoryIOException("L'item n'a pas été trouvé dans l'inventaire.", 2));

        System.out.println(foundItem.getMap());
        if (!AllowedItemTypeUtil.isAllowedForMain(foundItem)) {
            throw new InventoryIOException("Le type de l'item n'est pas autorisé dans le main slot.", 3);
        }

        items.remove(foundItem);

        if (mainSlot != null) {
            items.add(mainSlot);
        }

        mainSlot = foundItem;
    }

    public void pullFromMainSlot(boolean force) throws InventoryIOException {
        if (mainSlot == null) return;

        try {
            this.addItem(mainSlot);
            mainSlot = null;
        } catch (InventoryIOException e) {
            if (force) {
                mainSlot = null;
            } else {
                throw new InventoryIOException(e.getMessage(), 4);
            }
        }
    }

    public void pushToArmorSlot(ObjectId itemId) throws InventoryIOException {
        Item foundItem = items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new InventoryIOException("L'item n'a pas été trouvé dans l'inventaire.", 2));


        if (!AllowedItemTypeUtil.isAllowedForArmor(foundItem)) {
            throw new InventoryIOException("Le type de l'item n'est pas autorisé dans le armor slot.", 3);
        }

        items.remove(foundItem);

        if (armorSlot != null) {
            items.add(armorSlot);
        }

        armorSlot = foundItem;
    }

    public void pullFromArmorSlot(boolean force) throws InventoryIOException {
        if (armorSlot == null) return;

        try {
            this.addItem(armorSlot);
            armorSlot = null;
        } catch (InventoryIOException e) {
            if (force) {
                armorSlot = null;
            } else {
                throw new InventoryIOException(e.getMessage(), 4);
            }
        }
    }

    public void pushToSecondSlot(ObjectId itemId) throws InventoryIOException {
        Item foundItem = items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new InventoryIOException("L'item n'a pas été trouvé dans l'inventaire.", 2));

        if (!AllowedItemTypeUtil.isAllowedForSecond(foundItem)) {
            throw new InventoryIOException("Le type de l'item n'est pas autorisé dans le secondary slot.", 3);
        }

        items.remove(foundItem);

        if (secondSlot != null) {
            items.add(secondSlot);
        }

        secondSlot = foundItem;
    }

    public void pullFromSecondSlot(boolean force) throws InventoryIOException {
        if (secondSlot == null) return;

        try {
            this.addItem(secondSlot);
            secondSlot = null;
        } catch (InventoryIOException e) {
            if (force) {
                secondSlot = null;
            } else {
                throw new InventoryIOException(e.getMessage(), 4);
            }
        }
    }
}
