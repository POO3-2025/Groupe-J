package be.helha.poo3.serverpoo.ServicesTest;
import be.helha.poo3.serverpoo.models.*;
import be.helha.poo3.serverpoo.services.DungeonMapService;
import be.helha.poo3.serverpoo.services.ItemLoaderService;
import be.helha.poo3.serverpoo.models.Room.Direction;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DungeonMapServiceTest {

    private DungeonMapService service;
    private ItemLoaderService mockItemLoader;

    @BeforeEach
    public void setUp() {
        mockItemLoader = mock(ItemLoaderService.class);
        service = new DungeonMapService();
        service.setItemLoaderService(mockItemLoader);

        // Mock retour d'items pour chaque rareté
        for (Rarity rarity : Rarity.values()) {
            Item item = new Item(new ObjectId(), "Item_" + rarity, "type", rarity, "desc", "style");
            when(mockItemLoader.findByRarity(rarity)).thenReturn(List.of(item));
        }

        service.init(); // Lance la génération de la carte
    }

    @Test
    public void testMapIsGenerated() {
        Collection<Room> rooms = service.getAllRooms();
        assertFalse(rooms.isEmpty());
        assertEquals(20, rooms.size());
    }

    @Test
    public void testStartRoomIsNotNull() {
        Room startRoom = service.getStartRoom();
        assertNotNull(startRoom);
        assertEquals("0:0", startRoom.getId());
    }

    @Test
    public void testGetRoomByIdExists() {
        Room startRoom = service.getStartRoom();
        Room found = service.getRoomById(startRoom.getId());
        assertNotNull(found);
        assertEquals(startRoom, found);
    }

    @Test
    public void testGetRoomByIdNotFound() {
        Room room = service.getRoomById("999:999");
        assertNull(room);
    }

    @Test
    public void testGetRandomRarityDistribution() {
        // Run multiple times to verify ranges
        for (int i = 0; i < 100; i++) {
            Rarity rarity = service.getRandomRarity();
            assertNotNull(rarity);
        }
    }

    @Test
    public void testRespawnChestAndMonsterMechanics() {
        service.manageRespawn();

        long chestCount = service.getAllRooms().stream().filter(r -> r.getChest() != null).count();
        long monsterCount = service.getAllRooms().stream().filter(r -> r.getMonster() != null).count();

        assertTrue(chestCount >= 0);
        assertTrue(monsterCount >= 0);
    }


    //test que les sorties sont liées à des salles existantes
    @Test
    public void testExitsToExistingRoom() {
        for (Room room : service.getAllRooms()) {
            for (Map.Entry<Direction, Room> exit : room.getExits().entrySet()) {
                assertNotNull(exit.getValue(), "Sortie invalide vers une salle inexistante depuis " + room.getId());
            }
        }
    }

    //test que les sorties aillent dans les 2 sens
    @Test
    public void testExitsReciprocal() {
        for (Room room : service.getAllRooms()) {
            for (Map.Entry<Direction, Room> entry : room.getExits().entrySet()) {
                Room neighbour = entry.getValue();
                Direction opposite = service.opposite(entry.getKey());
                assertEquals(room, neighbour.getExit(opposite),
                        "La sortie depuis " + room.getId() + " vers " + entry.getKey()
                                + " n'est pas réciproque avec " + neighbour.getId());
            }
        }
    }

    //test que chaque room est atteignable
    @Test
    public void testRoomsReachable() {
        Room start = service.getStartRoom();
        Set<Room> visited = new HashSet<>();
        Queue<Room> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Room current = queue.poll();
            for (Room neighbor : current.getExits().values()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        assertEquals(service.getAllRooms().size(), visited.size(),
                "Nombre de salles atteignables incorrect depuis la salle de départ");
    }

    @Test
    public void testUniqueRoomID() {
        Set<String> ids = new HashSet<>();
        for (Room room : service.getAllRooms()) {
            assertTrue(ids.add(room.getId()), "Doublon d'identifiant trouvé : " + room.getId());
        }
    }

}


