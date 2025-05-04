package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;





import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DungeonMapService {
    @Autowired
    private ItemLoaderService itemLoaderService;
    private Room startRoom;
    private Map<Point, Room> roomGrid = new HashMap<>();
    private Random random = new Random();

    public void setItemLoaderService(ItemLoaderService itemLoaderService) {
        this.itemLoaderService = itemLoaderService;
    }

    /*
    Création de la map
     */

    @PostConstruct
    public void init(){
        generateMapWithCoordinates(20);
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

        addExtraConnections(0.25);
    }


    private void addExtraConnections(double prob) {
        for (Room room : roomGrid.values()) {
            Point p = new Point(room.getX(), room.getY());

            for (Room.Direction dir : Room.Direction.values()) {
                Point neighbourPos = move(p, dir);
                Room neighbour = roomGrid.get(neighbourPos);

                // mur présent ? (la pièce existe mais il n’y a pas d’exit)
                if (neighbour != null && room.getExit(dir) == null) {
                    if (random.nextDouble() < prob) {
                        connectRooms(room, neighbour, dir);  // crée l’ouverture
                    }
                }
            }
        }
    }

    private Room createRoom() {
        boolean hasMonster = random.nextBoolean();
        boolean hasChest = random.nextBoolean();

        Chest chest = null;
        Monster monster = null;

        //s'il y a un coffre, on choisit un rareté et on y insère un objet de la rareté choisie
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

        //s'il y a un monstre, génère une rareté aléatoire puis génère un monstre aléatoire dans cette rareté
        if(hasMonster){
            Rarity rarity = getRandomRarity(); // réutilise ta méthode existante
            monster = MonsterFactory.generateMonsterByRarity(rarity);

            if(monster == null){
                System.out.println("Aucun monstre trouvé pour la rareté : " + rarity);
            }
        }

        return new Room(hasMonster, hasChest, chest, monster);
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

    public Room.Direction opposite(Room.Direction dir) {
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


    /*
    gestion du respawn des coffres et des monstres
     */

    private void spawnChests(int count) {
        //vérifie que la salle n'a pas déjà un coffre
        List<Room> eligibleRooms = roomGrid.values().stream()
                .filter(room -> room.getChest() == null)
                .collect(Collectors.toList());
        Collections.shuffle(eligibleRooms);

        for (int i = 0; i < Math.min(count, eligibleRooms.size()); i++) {
            spawnChestInRoom(eligibleRooms.get(i));
        }
    }

    private void spawnChestInRoom(Room room) {
        Rarity rarity = getRandomRarity();
        List<Item> items = itemLoaderService.findByRarity(rarity);
        if (!items.isEmpty()) {
            Item item = items.get(random.nextInt(items.size()));
            room.setChest(new Chest(item));
            room.setHasChest(true);
        }
    }

    private void spawnMonsters(int count) {
        List<Room> eligibleRooms = roomGrid.values().stream()
                .filter(room -> room.getMonster() == null)
                .collect(Collectors.toList());
        Collections.shuffle(eligibleRooms);

        for (int i = 0; i < Math.min(count, eligibleRooms.size()); i++) {
            spawnMonsterInRoom(eligibleRooms.get(i));
        }
    }

    private void spawnMonsterInRoom(Room room) {
        room.setMonster(MonsterFactory.generateRandomMonster());
        room.setHasMonster(true);
    }

    //toutes les 60 secondes, on exécute cette méthode qui gère le respawn
    @Scheduled(fixedRate = 60000) // toutes les 60 secondes
    public void manageRespawn() {
        int totalRooms = roomGrid.size();
        int chestCount = 0;
        int monsterCount = 0;

        //compte le nombre de coffres et de monstres
        for (Room room : roomGrid.values()) {
            if (room.getChest() != null) chestCount++;
            if (room.getMonster() != null) monsterCount++;
        }
        //calcule le pourcentage de coffres et de monstres par rapport aux nombre de salles
        double chestRatio = (double) chestCount / totalRooms;
        double monsterRatio = (double) monsterCount / totalRooms;



            // force le spawn si le ratio est <30%
            if (chestRatio < 0.3) {
                int target = (int) (0.3 * totalRooms);
                if(target>chestCount) {
                    spawnChests(target - chestCount);
                }
                //s'il y a moins de 75% des salles qui ont un coffre
            } else if(chestRatio < 0.75) {
                //sinon, pour chaque salle, 10% de chance de spawn un chest si la salle n'en a pas déjà
                for (Room room : roomGrid.values()) {
                    if (room.getChest() == null && Math.random() < 0.1) {
                        spawnChestInRoom(room);
                    }
                }
            }


        // force le spawn si le ratio est <30%
        if (monsterRatio < 0.3) {
            int target = (int) (0.3 * totalRooms);
            if(target>monsterCount) {
                spawnMonsters(target - monsterCount);
            }
            //s'il y a moins de 75% des salles qui ont un coffre
        } else if(monsterRatio < 0.75) {
            //sinon, pour chaque salle, 10% de chance de spawn un monstre si la salle n'en a pas déjà
            for (Room room : roomGrid.values()) {
                if (room.getMonster() == null && Math.random() < 0.1) {
                    spawnMonsterInRoom(room);
                }
            }
        }
    }
}
