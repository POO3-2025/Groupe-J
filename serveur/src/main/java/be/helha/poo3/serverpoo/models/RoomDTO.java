package be.helha.poo3.serverpoo.models;

import java.util.List;
import java.util.stream.Collectors;

public class RoomDTO {
    private String id = null;
    private boolean hasChest = false;
    private boolean hasMonster = false;
    private Monster monster = null;
    private List<String> exits = null;
    private boolean fight=false;

    public RoomDTO(Room room) {
        this.id = room.getId();
        this.monster = room.getMonster();
        this.hasChest = (room.getChest() != null);
        this.hasMonster = (room.getMonster() != null);
        this.exits = room.getExits().keySet().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public RoomDTO(boolean fight) {
        this.fight=true;
    }

    // Getters nécessaires pour que Jackson sérialise correctement
    public String getId() { return id; }
    public boolean isHasChest() { return hasChest; }
    public boolean isHasMonster() { return hasMonster; }
    public Monster getMonster() { return monster; }
    public List<String> getExits() { return exits; }
    public boolean isFight(){ return fight; }
}

