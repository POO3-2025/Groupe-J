package be.helha.poo3.serverpoo.services;


import be.helha.poo3.serverpoo.utils.ConnexionMongoDB;
import be.helha.poo3.serverpoo.utils.DynamicClassGenerator;
import com.mongodb.client.MongoCollection;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemLoaderService {

    private final List<Object> loadedItems = new ArrayList<>();

    @PostConstruct
    public void init() {
        try{
            ConnexionMongoDB db = ConnexionMongoDB.getInstance();
            DynamicClassGenerator.getInstance().generate(db);
            MongoCollection<Document> collection = db.getCollection();

            for (Document doc : collection.find()) {
                String type = doc.getString("Type");
                Class<?> clazz = DynamicClassGenerator.getClasses().get(type);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                for (String key : doc.keySet()) {
                    if (key.equals("_id") || key.equals("Name") || key.equals("Type")) continue;
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
                    } catch (Exception e) {
                        System.out.println("Impossible de créer le champs : "+ key + "->" + e.getMessage());
                    }
                }

                loadedItems.add(instance);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des items : " + e.getMessage());
        }
    }

    public List<Object> getLoadedItems() {
        return loadedItems;
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
