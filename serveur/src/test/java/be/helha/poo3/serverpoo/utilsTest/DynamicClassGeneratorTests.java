package be.helha.poo3.serverpoo.utilsTest;

import be.helha.poo3.serverpoo.utils.ConnexionMongoDB;
import be.helha.poo3.serverpoo.utils.DynamicClassGenerator;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicClassGeneratorTests {
    static ConnexionMongoDB db;

    @BeforeAll
    static void init() {
        db = ConnexionMongoDB.getInstance();
        DynamicClassGenerator.getInstance().generate(db);
    }

    @Test
    public void testDynamicClassGenerationAndInstance() throws Exception {
        MongoCollection<Document> collection = db.getCollection();
        Document doc = collection.find().first();

        assertNotNull(doc, "Le document ne doit pas être null");

        String type = doc.getString("Type");
        assertNotNull(type, "Le champ 'Type' doit être présent");

        Map<String, Class<?>> generatedClasses = DynamicClassGenerator.getClasses();
        Class<?> clazz = generatedClasses.get(type);

        assertNotNull(clazz, "La classe dynamique pour le type '" + type + "' doit être générée");

        Object instance = clazz.getDeclaredConstructor().newInstance();
        assertNotNull(instance, "L'instance de la classe '" + type + "' ne doit pas être null");

        // Injecter les propriétés avec les setters
        for (String key : doc.keySet()) {
            if (key.equals("_id") || key.equals("Name") || key.equals("Type")) continue;

            Object value = doc.get(key);
            String methodName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);

            try {
                Method setter;
                try {
                    setter = clazz.getMethod(methodName, value.getClass());
                } catch (NoSuchMethodException e) {
                    // fallback pour primitives (int, double, etc.)
                    setter = findCompatibleSetter(clazz, methodName, value);
                }

                setter.invoke(instance, value);

                // Tester le getter
                Method getter = clazz.getMethod("get" + Character.toUpperCase(key.charAt(0)) + key.substring(1));
                Object returnedValue = getter.invoke(instance);

                assertEquals(value, returnedValue, "Le champ '" + key + "' doit être correctement set et get");

            } catch (NoSuchMethodException e) {
                fail("Méthode non trouvée pour le champ '" + key + "': " + e.getMessage());
            }
        }

        // Vérifie que toString contient le nom de classe et les valeurs
        String toString = instance.toString();
        assertTrue(toString.contains(type), "La méthode toString() doit contenir le nom de type");

        System.out.println("Objet généré dynamiquement : " + toString);
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

}

