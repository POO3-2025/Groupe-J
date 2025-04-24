package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.models.DungeonMap;
import be.helha.poo3.serverpoo.models.Room;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DungeonMapService {
    private final DungeonMap dungeonMap;

    public DungeonMapService() {
        this.dungeonMap = new DungeonMap(); // génération au lancement
    }

    public Room getStartRoom() {
        return dungeonMap.getStartRoom();
    }

    public Collection<Room> getAllRooms() {
        return dungeonMap.getAllRooms();
    }

    public Room getRoomById(String id) {
        return dungeonMap.getRoomById(id);
    }
}
