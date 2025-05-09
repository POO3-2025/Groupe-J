package be.helha.poo3.serverpoo.models;

import org.bson.types.ObjectId;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Item {
    @JsonProperty("_id")
    protected ObjectId id;
    protected String name;
    protected String type;
    protected String subType;
    protected Rarity rarity;
    protected String description;

    public Item() {
        this.name = "Basic Wood Sword";
        this.type = "Sword";
        this.subType = "Equipement"; // ou null
        this.id = new ObjectId();
        this.rarity = Rarity.uncommon;
        this.description = "Wood Sword";
    }

    public Item(ObjectId id, String name, String type, Rarity rarity, String description, String subType) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.subType = subType;
        this.rarity = rarity;
        this.description = description;
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

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", subType='" + subType + '\'' +
                ", rarity=" + rarity + '\'' +
                ", description='" + description +
                '}';
    }
    /*Test de getter et setter dynamique servant aux classes enfants*/
    public int getInt(String field) {
        try {
            String getterName = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
            Method getter;
            getter = this.getClass().getMethod(getterName);
            Object result = getter.invoke(this);
            return result instanceof Integer ? (Integer) result : null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean setInt(String field, int value) {
        String setterName = "set" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
        try {
            Method setter;
            try {
                setter = this.getClass().getMethod(setterName, int.class);
            } catch (NoSuchMethodException ex) {
                setter = this.getClass().getMethod(setterName, Integer.class);
            }
            setter.invoke(this, value);
            return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
