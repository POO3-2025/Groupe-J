package be.helha.poo3.serverpoo.services;


import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.models.Rarity;
import be.helha.poo3.serverpoo.components.ConnexionMongoDB;
import be.helha.poo3.serverpoo.components.DynamicClassGenerator;
import com.mongodb.client.MongoCollection;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
@Primary
@Service
public class ItemLoaderService {

    private final ConnexionMongoDB connexionMongoDB;
    private final DynamicClassGenerator classGenerator;

    private final List<Item> loadedItems = new ArrayList<>();

    public ItemLoaderService(ConnexionMongoDB connexionMongoDB, DynamicClassGenerator classGenerator) {
        this.connexionMongoDB = connexionMongoDB;
        this.classGenerator = classGenerator;
    }

    @PostConstruct
    public void init() {
        try{
            classGenerator.generate();
            MongoCollection<Document> collection = connexionMongoDB.getCollection("Items");

            for (Document doc : collection.find()) {
                String type = doc.getString("Type");
                Class<?> clazz = DynamicClassGenerator.getClasses().get(type);
                Item instance = (Item) clazz.getDeclaredConstructor().newInstance();
                for (String key : doc.keySet()) {
                    if (key.equals("_id")) {
                        Method setId = clazz.getMethod("setId", ObjectId.class);
                        setId.invoke(instance, doc.getObjectId("_id"));
                        continue;
                    }

                    if (key.equals("Rarity")){
                        Method setRarity = clazz.getMethod("setRarity", Rarity.class);
                        Rarity enumValue = Rarity.valueOf(doc.getString("Rarity"));
                        setRarity.invoke(instance, enumValue);
                        continue;
                    }

                    Object value = doc.get(key);
                    String methodName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);

                    try {
                        Method setter;
                        try {
                            setter = clazz.getMethod(methodName, value.getClass());
                        } catch (NoSuchMethodException e) {
                            setter = findCompatibleSetter(clazz, methodName, value);
                        }
                        setter.invoke(instance, value);
                    }
                    catch (Exception e) {
                        System.out.println("Impossible de créer le champs : "+ key + "->" + e.getMessage());
                    }
                }

                loadedItems.add(instance);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des items : " + e.getMessage());
        }
    }

    public List<Item> getLoadedItems() {
        return loadedItems;
    }

    public List<Item> findByName(String name) {
        return loadedItems.stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .toList();
    }

    public List<Item> findByType(String type) {
        return loadedItems.stream()
                .filter(item -> item.getType().equalsIgnoreCase(type))
                .toList();
    }

    public List<Item> findByRarity(Rarity rarity) {
        return loadedItems.stream()
                .filter(item -> item.getRarity() == rarity)
                .toList();
    }

    public List<Item> findByRarityAndType(Rarity rarity, String type) {
        return loadedItems.stream()
                .filter(item -> item.getRarity() == rarity && item.getType().equalsIgnoreCase(type))
                .toList();
    }

    public List<Item> findByRarityAndSubType(Rarity rarity, String subType) {
        return loadedItems.stream()
                .filter(item -> item.getRarity() == rarity && item.getSubType().equalsIgnoreCase(subType))
                .toList();
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
