package be.helha.poo3.serverpoo.models;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InventoryDTO {
    private ObjectId id;
    private List<Map<String, Object>> items;

    //private Map<String, Object> armorSlot;
    //private Map<String, Object> mainSlot;
    //private Map<String, Object> secondSlot;

    public InventoryDTO(Inventory inventory) {
        this.id = inventory.getId();
        this.items = inventory.getItems().stream()
                .map(Item::getMap)
                .collect(Collectors.toList());
        //this.armorSlot = inventory.getArmortSlot().getMap();
        //this.mainSlot = inventory.getMainSlot().getMap();
        //this.secondSlot = inventory.getSecondSlot().getMap();
    }
}