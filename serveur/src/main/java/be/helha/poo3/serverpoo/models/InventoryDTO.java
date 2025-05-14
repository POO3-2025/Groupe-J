package be.helha.poo3.serverpoo.models;

import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InventoryDTO {
    private ObjectId id;
    private List<Map<String, Object>> items;

    private Map<String, Object> armorSlot;
    private Map<String, Object> mainSlot;
    private Map<String, Object> secondSlot;

    public InventoryDTO(Inventory inventory) {
        this.id = inventory.getId();
        this.items = inventory.getItems().stream()
                .map(Item::getMap)
                .collect(Collectors.toList());
        this.armorSlot = inventory.getArmorSlot() != null ? inventory.getArmorSlot().getMap() : null;
        this.mainSlot = inventory.getMainSlot() != null ? inventory.getMainSlot().getMap() : null;
        this.secondSlot = inventory.getSecondSlot() != null ? inventory.getSecondSlot().getMap() : null;
    }

    @Override
    public String toString() {
        return "InventoryDTO{" +
                "id=" + id +
                ", items=" + items +
                ", armorSlot=" + (armorSlot != null ? armorSlot : "null") +
                ", mainSlot=" + (mainSlot != null ? mainSlot : "null") +
                ", secondSlot=" + (secondSlot != null ? secondSlot : "null") +
                '}';
    }
}