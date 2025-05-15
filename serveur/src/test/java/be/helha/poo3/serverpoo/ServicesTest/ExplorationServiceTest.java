package be.helha.poo3.serverpoo.ServicesTest;

import be.helha.poo3.serverpoo.models.*;
import be.helha.poo3.serverpoo.services.DungeonMapService;
import be.helha.poo3.serverpoo.services.ExplorationService;
import be.helha.poo3.serverpoo.services.InventoryService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExplorationServiceTest {

    @Mock
    private DungeonMapService dungeonMapService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ExplorationService explorationService;

    CharacterWithPos character;
    Item item;
    Room room;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        character = new CharacterWithPos(1,1,"michel","12345",100,75,2,2,1,new Point(0,0));
        item = new Item();
        room = new Room(true,true,new Chest(item),new Monster(Monsters.slime));
    }

    @Test
    void testGetCurrentRoom() {

        when(dungeonMapService.getRoomById("0:0")).thenReturn(room);

        RoomDTO result = explorationService.getCurrentRoom(character);

        assertNotNull(result);
        assertEquals("0:0", result.getId());
    }

    @Test
    void testMoveValidDirection() {

        Room currentRoom = mock(Room.class);
        Room nextRoom = new Room(true,true,new Chest(item),new Monster(Monsters.slime));

        when(dungeonMapService.getRoomById("0:0")).thenReturn(currentRoom);
        when(currentRoom.getExit(Room.Direction.NORTH)).thenReturn(nextRoom);

        Room result = explorationService.move(character, "NORTH");

        assertEquals(nextRoom, result);
    }

    @Test
    void testMoveInvalidDirection() {
        Room currentRoom = mock(Room.class);

        when(dungeonMapService.getRoomById("0:0")).thenReturn(currentRoom);
        when(currentRoom.getExit(Room.Direction.SOUTH)).thenReturn(null);

        Room result = explorationService.move(character, "SOUTH");

        assertNull(result);
    }

    @Test
    void testOpenChest() {

        character.setPosition(new Point(1, 1));
        Room room = mock(Room.class);
        Item item = new Item();

        when(dungeonMapService.getRoomById("1:1")).thenReturn(room);
        when(room.openChest()).thenReturn(item);

        Item result = explorationService.openChest(character);

        assertEquals(item, result);
    }

    @Test
    void testOpenChestNoRoom() {
        character.setPosition(new Point(1, 1));

        when(dungeonMapService.getRoomById("1:1")).thenReturn(null);

        Item result = explorationService.openChest(character);

        assertNull(result);
    }

    @Test
    void testGetLootFromChest() {
        character.setPosition(new Point(2, 2));
        character.setInventoryId(new ObjectId().toHexString());

        Room room = mock(Room.class);
        Item item = new Item();
        item.setId(new ObjectId());

        when(dungeonMapService.getRoomById("2:2")).thenReturn(room);
        when(room.openChest()).thenReturn(item);

        boolean result = explorationService.getLootFromChest(character);

        verify(inventoryService).addItemToInventory(new ObjectId(character.getInventoryId()), item.getId());
        verify(room).setChest(null);
        assertTrue(result);
    }

    @Test
    void testGetLootFromChestInvalidRoom() {
        character.setPosition(new Point(3, 3));

        when(dungeonMapService.getRoomById("3:3")).thenReturn(null);

        boolean result = explorationService.getLootFromChest(character);

        assertFalse(result);
    }

    @Test
    void testGetLootFromChestInvalidObjectId() {
        character.setPosition(new Point(4, 4));
        character.setInventoryId("invalid_id");

        Room room = mock(Room.class);
        Item item = new Item();

        when(dungeonMapService.getRoomById("4:4")).thenReturn(room);
        when(room.openChest()).thenReturn(item);

        assertThrows(IllegalArgumentException.class, () -> {
            explorationService.getLootFromChest(character);
        });
    }
}

