package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.models.Chest;
import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.models.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import be.helha.poo3.serverpoo.models.Rarity;
import jakarta.annotation.PostConstruct;




import java.awt.*;
import java.util.*;
import java.util.List;

@Service
public class DungeonMapService {
    @Autowired
    private ItemLoaderService itemLoaderService;
    private Room startRoom;
    private Map<Point, Room> roomGrid = new HashMap<>();
    private Random random = new Random();

    @PostConstruct
    public void init(){
        generateMapWithCoordinates(10);
    }

    private void generateMapWithCoordinates(int count) {
        Queue<Room> roomQueue = new LinkedList<>();
        Point origin = new Point(0, 0);
        //création de la première salle
        Room start = createRoom();
        start.setPosition(0, 0);
        roomGrid.put(origin, start);
        roomQueue.add(start);

        while (roomGrid.size() < count && !roomQueue.isEmpty()) {
            //poll() renvoit et retire le premier element de roomQueue , permet de récupérer la salle actuelle
            Room current = roomQueue.poll();
            Point currentPos = new Point(current.getX(), current.getY());

            //récupère toutes les directions et les mélanges
            List<Room.Direction> directions = new ArrayList<>(Arrays.asList(Room.Direction.values()));
            Collections.shuffle(directions);

            for (Room.Direction dir : directions) {
                if (roomGrid.size() >= count) break;
                //on se déplace dans une direction à partir de la salle actuelle
                Point newPos = move(currentPos, dir);
                if (!roomGrid.containsKey(newPos)) {
                    //création d'une nouvelle salle à la nouvelle position
                    Room newRoom = createRoom();
                    newRoom.setPosition(newPos.x, newPos.y);
                    connectRooms(current, newRoom, dir);

                    roomGrid.put(newPos, newRoom);
                    roomQueue.add(newRoom);
                }
            }
        }
        startRoom = start;
    }

    private Room createRoom() {
        boolean hasMonster = random.nextBoolean();
        boolean hasChest = random.nextBoolean();

        Chest chest = null;

        if (hasChest) {
            Rarity chosenRarity = getRandomRarity();
            List<Item> items = itemLoaderService.findByRarity(chosenRarity);

            if (items != null && !items.isEmpty()) {
                Item randomItem = items.get(random.nextInt(items.size()));
                chest = new Chest(randomItem);
            } else {
                chest = null;
                System.out.println("Aucun item trouvé pour la rareté : " + chosenRarity);
            }
        }

        return new Room(hasMonster, hasChest, chest);
    }

    private void connectRooms(Room a, Room b, Room.Direction dir) {
        //lie deux salle en settant la sortie de la salle a avec la direction et lasortie de la salle b avec la direction opposée
        Room.Direction opposite = opposite(dir);
        a.setExit(dir, b);
        b.setExit(opposite, a);
    }

    private Point move(Point pos, Room.Direction dir) {
        //modifie les coordonnées x et y en fonction de la direction que l'on prend
        switch (dir) {
            case NORTH: return new Point(pos.x, pos.y + 1);
            case SOUTH: return new Point(pos.x, pos.y - 1);
            case EAST:  return new Point(pos.x + 1, pos.y);
            case WEST:  return new Point(pos.x - 1, pos.y);
        }
        return pos;
    }

    private Room.Direction opposite(Room.Direction dir) {
        switch (dir) {
            case NORTH: return Room.Direction.SOUTH;
            case SOUTH: return Room.Direction.NORTH;
            case EAST:  return Room.Direction.WEST;
            case WEST:  return Room.Direction.EAST;
        }
        return null;
    }

    public Room getStartRoom() {
        return startRoom;
    }

    public Collection<Room> getAllRooms(){
        return  roomGrid.values();
    }

    public Room getRoomById(String id) {
        //récupération des coordonnées à partir de l'id pour créer un Point
        String[] coords = id.split(":");
        Point point = new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));

        return roomGrid.getOrDefault(point, null);
    }

    //génère une rareté semi-aléatoire, plus la rareté est élevée, moins elle a de chance d'apparaître
    public Rarity getRandomRarity() {
        double randomValue = Math.random(); // entre 0.0 et 1.0

        if (randomValue < 0.5) return Rarity.common;         // 50%
        else if (randomValue < 0.75) return Rarity.uncommon; // 25%
        else if (randomValue < 0.9) return Rarity.rare;      // 15%
        else if (randomValue < 0.98) return Rarity.epic;     // 8%
        else return Rarity.legendary;                        // 2%
    }
}
