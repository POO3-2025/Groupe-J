package be.helha.poo3.serverpoo.models;

import java.util.List;
import java.util.stream.Collectors;

public class RoomDTO {
    private String id;
    private boolean hasChest;
    private boolean hasMonster;
    private Chest chest;
    private Monster monster;
    private List<String> exits;

    public RoomDTO(Room room) {
        this.id = room.getId();
        this.chest = room.getChest();
        this.monster = room.getMonster();
        this.hasChest = (room.getChest() != null);
        this.hasMonster = (room.getMonster() != null);
        this.exits = room.getExits().keySet().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    // Getters nécessaires pour que Jackson sérialise correctement
    public String getId() { return id; }
    public boolean isHasChest() { return hasChest; }
    public boolean isHasMonster() { return hasMonster; }
    public Chest getChest() { return chest; }
    public Monster getMonster() { return monster; }
    public List<String> getExits() { return exits; }
}

