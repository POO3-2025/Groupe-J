package be.helha.poo3.services;

import be.helha.poo3.models.Item;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    @BeforeEach
    void setUp() throws Exception {
        ItemService.initializeFromJsonFile("src/test/resources/test_items.json");
    }

    @Test
    void testLoadAllItems() {
        List<Item> items = ItemService.getAllItems();
        assertNotNull(items, "La liste des items ne doit pas être nulle");
        assertEquals(4, items.size(), "Il doit y avoir 3 items chargés");
    }

    @Test
    void testGetItemByName() {
        Item sword = ItemService.getItemByName("Basic Sword");
        assertNotNull(sword, "Basic Sword doit être trouvé");
        assertEquals("Sword", sword.getType(), "Le type de Basic Sword doit être 'Sword'");
    }

    @Test
    void testGetItemById() {
        String knownId = "67bf371dc5340129f6b02b2f";
        Item item = ItemService.getItemById(knownId);
        assertNotNull(item, "L'item avec l'ID connu doit être trouvé");
        assertEquals("Basic Sword", item.getName(), "Le nom de l'item doit être 'Basic Sword'");
    }

    @Test
    void testGetItemByIdInvalid() {
        String fakeId = new ObjectId().toHexString();
        Item item = ItemService.getItemById(fakeId);
        assertNull(item, "Aucun item ne doit être trouvé pour un ID inconnu");
    }

    @Test
    void testDynamicAttribute() throws Exception {
        Item sword = ItemService.getItemByName("Basic Sword");
        assertNotNull(sword);

        Method getDamage = sword.getClass().getMethod("getDamage");
        Object value = getDamage.invoke(sword);

        assertEquals(50, value);
    }

    @Test
    public void dynamicItemIntGetterAndSetter() throws Exception {
        Item item = ItemService.getItemByName("Basic Sword");
        assertNotNull(item);
        int value = item.getInt("damage");
        assertEquals(50, value);
        item.setInt("damage", 20);
        value = item.getInt("damage");
        assertEquals(20, value);
    }

    @Test
    public void checkAttributesList() throws Exception {
        Item item = ItemService.getItemByName("Basic Sword");
        assertNotNull(item);
        List<String> attributes = item.getAdditionalAttributes();
        assertEquals(1, attributes.size(),"Il doit y avoir exactement un attribut supplémentaire");
        assertTrue(attributes.contains("damage"), "L'attribut « damage » est attendu");
    }



}
