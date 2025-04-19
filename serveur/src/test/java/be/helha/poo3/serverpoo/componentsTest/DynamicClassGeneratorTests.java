package be.helha.poo3.serverpoo.utilcomponentsTest;

import be.helha.poo3.serverpoo.components.DynamicClassGenerator;
import be.helha.poo3.serverpoo.models.Rarity;
import be.helha.poo3.serverpoo.components.ConnexionMongoDB;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class DynamicClassGeneratorTests {

    @Autowired
    private DynamicClassGenerator generator;

    @Autowired
    private ConnexionMongoDB mockDb;

    private static Document doc;

    @BeforeEach
    public void setup() {
        generator.generate();
    }

    @Test
    public void testDynamicClassGenerationAndInstance() throws Exception {
        String type = doc.getString("Type");
        assertNotNull(type);

        Map<String, Class<?>> generated = DynamicClassGenerator.getClasses();
        assertTrue(generated.containsKey(type), "La classe doit être générée pour le type " + type);

        Class<?> clazz = generated.get(type);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        assertNotNull(instance);

        for (String key : doc.keySet()) {
            if (key.equals("_id") || key.equals("Name") || key.equals("Type")) continue;

            Object value = doc.get(key);
            String methodSuffix = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            String setterName = "set" + methodSuffix;
            String getterName = "get" + methodSuffix;

            try {
                Method setter;
                if (key.equals("Rarity")) {
                    setter = clazz.getMethod(setterName, Rarity.class);
                    setter.invoke(instance, Rarity.valueOf(value.toString()));
                } else {
                    try {
                        setter = clazz.getMethod(setterName, value.getClass());
                    } catch (NoSuchMethodException e) {
                        setter = findCompatibleSetter(clazz, setterName, value);
                    }
                    setter.invoke(instance, value);
                }

                Method getter = clazz.getMethod(getterName);
                Object returnedValue = getter.invoke(instance);

                if (key.equals("Rarity")) {
                    assertEquals(value.toString(), returnedValue.toString(), "Le champ 'Rarity' doit être correctement set et get");
                } else {
                    assertEquals(value, returnedValue, "Le champ '" + key + "' doit être correctement set et get");
                }

            } catch (NoSuchMethodException e) {
                fail("Méthode manquante pour le champ '" + key + "': " + e.getMessage());
            }
        }

        String result = instance.toString();
        assertTrue(result.contains(type), "Le toString() doit contenir le nom de la classe");
        System.out.println("Objet mocké généré : " + result);
    }

    private Method findCompatibleSetter(Class<?> clazz, String methodName, Object value) throws Exception {
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName)) continue;
            Class<?> paramType = method.getParameterTypes()[0];
            if (paramType.isPrimitive()) {
                if ((paramType == int.class && value instanceof Integer)
                        || (paramType == double.class && value instanceof Double)
                        || (paramType == boolean.class && value instanceof Boolean)) {
                    return method;
                }
            }
        }
        throw new NoSuchMethodException(clazz.getName() + "." + methodName + "(" + value.getClass() + ")");
    }

    @TestConfiguration
    static class MockMongoConfig {

        @Bean
        public ConnexionMongoDB mockDb() {
            MongoCollection<Document> mockCollection = mock(MongoCollection.class);
            FindIterable<Document> mockIterable = mock(FindIterable.class);
            MongoCursor<Document> mockCursor = mock(MongoCursor.class);

            Document doc = new Document()
                    .append("_id", new ObjectId())
                    .append("Name", "Test Sword")
                    .append("Type", "Sword")
                    .append("Rarity", "rare")
                    .append("Description", "A good sword")
                    .append("Damage", 51);

            DynamicClassGeneratorTests.doc = doc;

            when(mockCursor.hasNext()).thenReturn(true, false);
            when(mockCursor.next()).thenReturn(doc);
            when(mockIterable.iterator()).thenReturn(mockCursor);
            when(mockCollection.find()).thenReturn(mockIterable);

            ConnexionMongoDB db = mock(ConnexionMongoDB.class);
            when(db.getCollection()).thenReturn(mockCollection);

            return db;
        }
    }
}

