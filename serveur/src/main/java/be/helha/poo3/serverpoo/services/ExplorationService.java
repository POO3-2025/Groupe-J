package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.models.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.helha.poo3.serverpoo.services.DungeonMapService;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExplorationService {

    private final DungeonMapService dungeonMapService;
    private final InventoryService inventoryService;

    public ExplorationService(DungeonMapService dungeonMapService, InventoryService inventoryService) {
        this.dungeonMapService = dungeonMapService;
        this.inventoryService = inventoryService;
    }

    public RoomDTO getCurrentRoom(CharacterWithPos character) {
        // si le joueur n'a pas encore de position, on le met dans la salle de départ
        Point position = character.getPosition();
        Room room = dungeonMapService.getRoomById(position.x + ":" + position.y);
        return new RoomDTO(room);
    }

    public Room move(CharacterWithPos character, String direction) {
        Point position = character.getPosition();;
        Room currentRoom = dungeonMapService.getRoomById(position.x + ":" + position.y);
        if (currentRoom == null) {
            currentRoom = dungeonMapService.getStartRoom();
        }
            Room.Direction dir = Room.Direction.valueOf(direction.toUpperCase());
            Room nextRoom = currentRoom.getExit(dir);
            if (nextRoom != null) {
                //utiliser la méthode pour mettre à jour la position d'un personnage en fct de l'id
                return nextRoom;
            } else {
                // Pas de sortie dans cette direction → reste dans la même salle
                return null;
            }
    }

    public Item openChest(CharacterWithPos character) {
        Point position = character.getPosition();
        Room room = dungeonMapService.getRoomById(position.x + ":" + position.y);
        if (room == null) {
            return null;
        }else {
           Item item =  room.openChest();
           return item;
        }
    }

    public boolean getLootFromChest(CharacterWithPos character) {
        Point position = character.getPosition();
        Room room = dungeonMapService.getRoomById(position.x + ":" + position.y);
        if (room == null) {
            return false;
        }else {
            Item item =  room.openChest();
            if (!ObjectId.isValid(character.getInventoryId())) {
                throw new IllegalArgumentException("L'ID d'inventaire n'est pas un ObjectId valide.");
            }
            ObjectId inventoryID = new ObjectId(character.getInventoryId());
            System.out.println("inventaire : " + inventoryID + " item : " + item.getId());
            inventoryService.addItemToInventory(inventoryID,item.getId());
            room.setChest(null);
            return true;
        }
    }
}