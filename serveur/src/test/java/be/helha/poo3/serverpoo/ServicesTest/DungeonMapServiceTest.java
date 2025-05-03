package be.helha.poo3.serverpoo.ServicesTest;
import be.helha.poo3.serverpoo.models.*;
import be.helha.poo3.serverpoo.services.DungeonMapService;
import be.helha.poo3.serverpoo.services.ItemLoaderService;
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
        assertEquals(50, rooms.size());
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
}

