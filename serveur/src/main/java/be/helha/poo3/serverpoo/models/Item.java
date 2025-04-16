package be.helha.poo3.serverpoo.models;

import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {
    @JsonProperty("_id")
    protected ObjectId id;
    protected String name;
    protected String type;
    protected Rarity rarity;
    protected String description;

    public Item() {
        this.name = "Basic Wood Sword";
        this.type = "Sword";
        this.id = new ObjectId();
        this.rarity = Rarity.uncommon;
    }

    public Item(ObjectId id, String name, String type, Rarity rarity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rarity = rarity;
    }
    public static class ObjectIdWrapper {
        @JsonProperty("$oid")
        private String oid;

        public String getOid() { return oid; }
        public void setOid(String oid) { this.oid = oid; }

        @Override
        public String toString() {
            return oid;
        }
    }
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", rarity=" + rarity +
                '}';
    }
}
