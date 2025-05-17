package be.helha.poo3.serverpoo.services;


import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.models.Rarity;
import be.helha.poo3.serverpoo.components.ConnexionMongoDB;
import be.helha.poo3.serverpoo.components.DynamicClassGenerator;
import com.mongodb.client.MongoCollection;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
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
                    switch (key){
                        case "_id":
                            instance.setId(doc.getObjectId(key));
                            break;
                        case "Name":
                            instance.setName(doc.getString(key));
                            break;
                        case "Type":
                            instance.setType(doc.getString(key));
                            break;
                        case "SubType":
                            instance.setSubType(doc.getString(key));
                            break;
                        case "Rarity":
                            instance.setRarity(Rarity.valueOf(doc.getString(key)));
                            break;
                        case "Description":
                            instance.setDescription(doc.getString(key));
                            break;
                        default:
                            Object val = doc.get(key);
                            if (val instanceof Number n) {                          // JSON -> nombre
                                instance.setInt(key, n.intValue());
                            } else if (val instanceof String s && !s.isBlank()) {   // JSON -> chaîne
                                instance.setInt(key, Integer.parseInt(s));
                            }
                            break;
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

    public Item findByName(String name) {
        return loadedItems.stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
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
