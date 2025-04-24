package be.helha.poo3.serverpoo.componentsTest;

import be.helha.poo3.serverpoo.components.DynamicClassGenerator;
import be.helha.poo3.serverpoo.config.TestMongoConfig;
import be.helha.poo3.serverpoo.models.Rarity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = TestMongoConfig.class)
@ActiveProfiles("test")
public class DynamicClassGeneratorTests {

    @Autowired
    private DynamicClassGenerator generator;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static Document doc;

    @BeforeEach
    public void setup() throws Exception {
        mongoTemplate.dropCollection("Items");

        InputStream is = getClass().getClassLoader().getResourceAsStream("TestDB.MaCollection.json");
        assertNotNull(is, "Le fichier JSON de test n'a pas été trouvé");

        // Lire le fichier entier
        String jsonArray = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Encapsuler pour que Mongo le parse comme une liste de documents
        Document wrapper = Document.parse("{\"array\": " + jsonArray + "}");
        List<Document> documents = wrapper.getList("array", Document.class);

        mongoTemplate.insert(documents, "Items");

        doc = documents.get(0);
        generator.generate();
    }

    @Test
    public void testDynamicClassGenerationAndInstance() throws Exception {
        String type = doc.getString("Type");
        System.out.println("🔍 Document utilisé : " + doc.toJson());
        System.out.println("📦 Type extrait du document : " + type);
        assertNotNull(type);

        Map<String, Class<?>> generated = DynamicClassGenerator.getClasses();


        System.out.println("📚 Types générés : " + generated.keySet());
        assertTrue(generated.containsKey(type),
                "❌ Aucune classe générée pour le type '" + type + "'. " +
                        "Types disponibles : " + generated.keySet());

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
}

