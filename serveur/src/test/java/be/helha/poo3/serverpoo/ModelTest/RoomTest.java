package be.helha.poo3.serverpoo.ModelTest;
import static org.junit.jupiter.api.Assertions.*;

import be.helha.poo3.serverpoo.models.*;
import be.helha.poo3.serverpoo.models.Room.Direction;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RoomTest {

    private Item item;
    private Chest chest;
    private Monster monster;
    private Room room;

    @BeforeEach
    public void setUp() {
        item = new Item(new ObjectId(), "Sword", "Weapon", Rarity.common, "A sharp blade", "Melee");
        chest = new Chest(item);
        monster = new Monster(Monsters.goblin);
        room = new Room(true, true, chest, monster);
        room.setPosition(2, 3);
    }

    @Test
    public void testRoomInitialization() {
        assertEquals("2:3", room.getId());
        assertEquals(2, room.getX());
        assertEquals(3, room.getY());
        assertTrue(room.getHasChest());
        assertTrue(room.getHasMonster());
        assertNotNull(room.getChest());
        assertNotNull(room.getMonster());
    }

    @Test
    public void testOpenChest() {
        Item result = room.openChest();
        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
    }

    @Test
    public void testSetAndGetExits() {
        Room northRoom = new Room(false, false, null, null);
        northRoom.setPosition(2, 2);
        room.setExit(Direction.NORTH, northRoom);

        assertEquals(northRoom, room.getExit(Direction.NORTH));
        assertTrue(room.getExits().containsKey(Direction.NORTH));
    }

    @Test
    public void testSetChestAndMonster() {
        Chest newChest = new Chest(new Item(new ObjectId(), "Bow", "Weapon", Rarity.rare, "Long-range bow", "Ranged"));
        Monster newMonster = new Monster(Monsters.orc);

        room.setChest(newChest);
        room.setMonster(newMonster);

        assertEquals("Bow", room.getChest().getItem().getName());
        assertEquals("Orc", room.getMonster().getName());
    }

    @Test
    public void testToString() {
        String output = room.toString();
        assertTrue(output.contains("Room{"));
        assertTrue(output.contains("id='2:3'"));
        assertTrue(output.contains("le coffre contient un(e)"));
    }
}
