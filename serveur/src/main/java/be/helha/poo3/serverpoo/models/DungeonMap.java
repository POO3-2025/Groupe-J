package be.helha.poo3.serverpoo.models;

import be.helha.poo3.serverpoo.models.Room;

import java.awt.Point;
import java.util.*;


public class DungeonMap {
    private Room startRoom;
    private Map<Point, Room> roomGrid = new HashMap<>();
    private Random random = new Random();

    public DungeonMap() {
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
        return new Room(hasMonster, hasChest);
    }

    private void connectRooms(Room a, Room b, Room.Direction dir) {
        //lie deux salle en settant la salle a avec la direction et la salle b avec la direction opposée
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

    public void printMap() {
        System.out.println("Carte du donjon :");
        for (Map.Entry<Point, Room> entry : roomGrid.entrySet()) {
            Point p = entry.getKey();
            Room r = entry.getValue();
            System.out.println("Salle \"" + r.getId() + "\" à (" + p.x + ", " + p.y + ")");
        }
    }
}
