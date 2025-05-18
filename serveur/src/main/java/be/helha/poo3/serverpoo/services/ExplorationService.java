package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.models.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.helha.poo3.serverpoo.services.DungeonMapService;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Service d'exploration permettant la navigation d'un personnage dans le donjon.
 * Gère la récupération des salles courantes, les déplacements, l'ouverture des coffres et la collecte d'objets.
 */
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

    /**
     * Déplace un personnage dans une direction donnée si possible.
     * @param character personnage à déplacer
     * @param direction direction de déplacement
     * @return nouvelle salle après déplacement, ou null si mouvement impossible
     */
    public Room move(CharacterWithPos character, String direction) {
        Point position = character.getPosition();
        Room currentRoom = dungeonMapService.getRoomById(position.x + ":" + position.y);
        if (currentRoom == null) {
            currentRoom = dungeonMapService.getStartRoom();
        }

        Room.Direction dir = Room.Direction.valueOf(direction.toUpperCase());
        Room nextRoom = currentRoom.getExit(dir);

        if (nextRoom != null) {
            // Met à jour la position du personnage
            switch (dir) {
                case NORTH -> character.setPosition(new Point(position.x, position.y + 1));
                case SOUTH -> character.setPosition(new Point(position.x, position.y - 1));
                case EAST  -> character.setPosition(new Point(position.x + 1, position.y));
                case WEST  -> character.setPosition(new Point(position.x - 1, position.y));
            }
            return nextRoom;
        } else {
            return null;
        }
    }

    /**
     * Permet à un personnage d'ouvrir un coffre dans la salle courante.
     * Si un coffre est présent, l'objet est transféré dans l'inventaire du joueur et le coffre est supprimé.
     * @param character personnage qui ouvre le coffre
     * @return true si coffre ouvert et objet récupéré, false sinon
     */
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

    /**
     * Permet à un personnage de récupérer l'objet contenu dans un coffre.
     * @param character personnage qui ouvre le coffre
     * @return true si l'objet a bien été récupéré, false si l'inventaire est pleins
     */
    public boolean getLootFromChest(CharacterWithPos character) {
        Point position = character.getPosition();
        Room room = dungeonMapService.getRoomById(position.x + ":" + position.y);
        //vérifie que l'inventaire n'est pas plein
        Inventory inventory = inventoryService.getInventory(new ObjectId(character.getInventoryId()));
        if(inventory.getItems().size() >= 10){
            return false;
        }

        if (room == null) {
            return false;
        }else {
            Item item =  room.openChest();
            if (!ObjectId.isValid(character.getInventoryId())) {
                throw new IllegalArgumentException("L'ID d'inventaire n'est pas un ObjectId valide.");
            }
            ObjectId inventoryID = new ObjectId(character.getInventoryId());
            inventoryService.addItemToInventory(inventoryID,item.getId());
            room.setChest(null);
            return true;
        }
    }
}