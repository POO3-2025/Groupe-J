package be.helha.poo3.serverpoo.models;

import java.util.List;
import java.util.stream.Collectors;

public class RoomDTO {
    private String id;
    private boolean hasChest;
    private boolean hasMonster;
    private List<String> exits;

    public RoomDTO(Room room) {
        this.id = room.getId();
        this.hasChest = room.hasChest();
        this.hasMonster = room.hasMonster();
        this.exits = room.getExits().keySet().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    // Getters nécessaires pour que Jackson sérialise correctement
    public String getId() { return id; }
    public boolean isHasChest() { return hasChest; }
    public boolean isHasMonster() { return hasMonster; }
    public List<String> getExits() { return exits; }
}

