package be.helha.poo3.serverpoo.servicesTest;

import be.helha.poo3.serverpoo.config.TestMongoConfig;
import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.models.Rarity;
import be.helha.poo3.serverpoo.services.ItemLoaderService;
import be.helha.poo3.serverpoo.components.DynamicClassGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestMongoConfig.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ItemLoaderServiceTest {

    @Autowired
    private ItemLoaderService service;

    @Autowired
    private DynamicClassGenerator generator;

    @Test
    public void shouldLoadItemsWithExpectedFields() throws Exception {
        List<Item> items = service.getLoadedItems();
        assertEquals(4, items.size(), "Il doit y avoir exactement 4 items chargés");

        for (Item item : items) {
            switch (item.getName()) {
                case "Basic Sword" -> {
                    assertEquals("Sword", item.getType());
                    assertEquals(Rarity.common, item.getRarity());
                    assertEquals("A simple sword", item.getDescription());
                    assertEquals(50, getFieldValue(item, "damage"));
                }
                case "Basic Shield" -> {
                    assertEquals("Shield", item.getType());
                    assertEquals(Rarity.common, item.getRarity());
                    assertEquals("A simple Shield", item.getDescription());
                    assertEquals(20, getFieldValue(item, "defense"));
                }
                case "Upgraded Shield" -> {
                    assertEquals("Shield", item.getType());
                    assertEquals(Rarity.uncommon, item.getRarity());
                    assertEquals("A more robust shield", item.getDescription());
                    assertEquals(30, getFieldValue(item, "defense"));
                }
                case "Small potion" -> {
                    assertEquals("Potion", item.getType());
                    assertEquals(Rarity.common, item.getRarity());
                    assertEquals("A basic healing potion", item.getDescription());
                    assertEquals("Consumable", item.getSubType());
                    assertEquals(10, getFieldValue(item, "capacity"));
                    assertEquals(10, getFieldValue(item, "currentCapacity"));
                }
                default -> fail("Nom d’item inconnu : " + item.getName());
            }
        }
    }

    @Test
    public void shouldFindItemByName() {
        Item result = service.findByName("Basic Sword");
        assertNotNull(result, "Aucun objet trouvé avec le nom 'Basic Sword'");
        assertEquals("Basic Sword", result.getName());
    }

    @Test
    public void shouldFindItemsByType() {
        List<Item> result = service.findByType("Sword");
        assertFalse(result.isEmpty(), "Un ou plusieurs objets doivent avoir le type 'Sword'");
        for (Item item : result) {
            assertEquals("Sword", item.getType());
        }
    }

    @Test
    public void shouldFindItemsByRarity() {
        List<Item> result = service.findByRarity(Rarity.common);
        assertFalse(result.isEmpty(), "Un ou plusieurs objets doivent avoir la rareté 'common'");
        for (Item item : result) {
            assertEquals(Rarity.common, item.getRarity());
        }
    }

    @Test
    public void shouldFindItemsByRarityAndType() {
        List<Item> result = service.findByRarityAndType(Rarity.common, "Sword");
        assertFalse(result.isEmpty(), "Un ou plusieurs objets doivent avoir la rareté 'common' et le type 'Sword'");
        for (Item item : result) {
            assertEquals(Rarity.common, item.getRarity());
            assertEquals("Sword", item.getType());
        }
    }

    @Test
    public void shouldFindItemsByRarityAndSubType() {
        List<Item> result = service.findByRarityAndSubType(Rarity.common, "Equipement");
        assertFalse(result.isEmpty(), "Un ou plusieurs objets doivent avoir la rareté 'common' et le subType 'Equipement'");
        for (Item item : result) {
            assertEquals(Rarity.common, item.getRarity());
            assertEquals("Equipement", item.getSubType());
        }
    }

    @Test
    public void shouldContainDynamicField() throws Exception {
        List<Item> items = service.getLoadedItems();
        Item item = items.get(0);

        try {
            Object damage = getFieldValue(item, "damage");
            assertNotNull(damage, "Le champ dynamique 'damage' devrait exister");
        } catch (NoSuchMethodException e) {
            fail("Le champ dynamique 'damage' est attendu mais manquant.");
        }
    }

    @Test
    @DirtiesContext
    public void dynamicItemIntGetterAndSetter() throws Exception {
        Item item = service.findByName("Basic Sword");
        assertNotNull(item);
        int value = item.getInt("damage");
        assertEquals(50, value);
        item.setInt("damage", 20);
        value = item.getInt("damage");
        assertEquals(20, value);
    }

    @Test
    public void checkAttributesList() throws Exception {
        Item item = service.findByName("Basic Sword");
        assertNotNull(item);
        List<String> attributes = item.getAdditionalAttributes();
        assertEquals(1, attributes.size(),"Il doit y avoir exactement un attribut supplémentaire");
        assertTrue(attributes.contains("damage"), "L'attribut « damage » est attendu");
    }

    private Object getFieldValue(Object obj, String field) throws Exception {
        String methodName = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
        Method method = obj.getClass().getMethod(methodName);
        return method.invoke(obj);
    }
}
