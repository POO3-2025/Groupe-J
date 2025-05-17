package be.helha.poo3.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomDTOClient {
    private String id = null;
    private boolean hasChest = false;
    private boolean hasMonster = false;
    private Monster monster = null;
    private List<String> exits = null;
    private boolean fight=false;

    public RoomDTOClient() {
    }

    public String getId() {
        return id;
    }

    public List<String> getExits() {
        return exits;
    }

    public boolean isHasChest() {
        return hasChest;
    }

    public boolean isHasMonster() {
        return hasMonster;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExits(List<String> exits) {
        this.exits = exits;
    }

    public void setHasChest(boolean hasChest) {
        this.hasChest = hasChest;
    }

    public void setHasMonster(boolean hasMonster) {
        this.hasMonster = hasMonster;
    }

    public void setMonster(Monster monster) { this.monster = monster; }

    public Monster getMonster() { return monster; }

}
