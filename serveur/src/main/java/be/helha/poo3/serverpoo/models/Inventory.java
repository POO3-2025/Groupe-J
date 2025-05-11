package be.helha.poo3.serverpoo.models;

import com.google.gson.annotations.SerializedName;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

public class Inventory {
    @SerializedName("_id")
    private ObjectId id;
    private List<Item> items;

    //private Item armorSlot;
    //private Item mainSlot;
    //private Item secondSlot;

    public Inventory() {
        this.id = new ObjectId();
        this.items = new ArrayList<>();
    }

    public Inventory(ObjectId id, List<Item> items) {
        this.id = id;
        this.items = items != null ? items : new ArrayList<>();
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

    public void addItem(Item item) {
        if (this.items == null) this.items = new ArrayList<>();
        this.items.add(item);
    }

    public void removeItem(Item item) {
        if (this.items != null) this.items.remove(item);
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "id=" + id +
                ", items=" + items +
                '}';
    }
}
