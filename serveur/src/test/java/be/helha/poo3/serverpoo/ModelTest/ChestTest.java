package be.helha.poo3.serverpoo.ModelTest;

import static org.junit.jupiter.api.Assertions.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import be.helha.poo3.serverpoo.models.*;

public class ChestTest {

    @Test
    public void testOpen() {
        ObjectId id = new ObjectId();
        Item testItem = new Item(id, "Epic Axe", "Axe", Rarity.rare, "A mighty axe", "Weapon");
        Chest chest = new Chest(testItem);

        Item result = chest.open();

        assertNotNull(result);
        assertEquals(testItem.getId(), result.getId());
        assertEquals(testItem.getName(), result.getName());
        assertEquals(testItem.getType(), result.getType());
        assertEquals(testItem.getSubType(), result.getSubType());
        assertEquals(testItem.getRarity(), result.getRarity());
        assertEquals(testItem.getDescription(), result.getDescription());
    }

    @Test
    public void testGetItem() {
        ObjectId id = new ObjectId();
        Item testItem = new Item(id, "Magic Wand", "Wand", Rarity.legendary, "A powerful wand", "Magic");
        Chest chest = new Chest(testItem);

        Item result = chest.getItem();

        assertNotNull(result);
        assertEquals(testItem.getId(), result.getId());
        assertEquals(testItem.getName(), result.getName());
        assertEquals(testItem.getType(), result.getType());
        assertEquals(testItem.getSubType(), result.getSubType());
        assertEquals(testItem.getRarity(), result.getRarity());
        assertEquals(testItem.getDescription(), result.getDescription());
    }
}

