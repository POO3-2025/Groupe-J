package be.helha.poo3.serverpoo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import javassist.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DynamicClassGenerator {
    private static DynamicClassGenerator instance;
    private static final Map<String, Class<?>> classes = new HashMap<>();
    private static final String BASE_PACKAGE = "be.helha.poo3.serverpoo.models";
    private DynamicClassGenerator() {}

    public static DynamicClassGenerator getInstance() {
        if (instance == null) {
            instance = new DynamicClassGenerator();
        }
        return instance;
    }

    public void generate(ConnexionMongoDB db) {
        try (MongoCursor<Document> cursor = db.getCollection().find().iterator()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Document> documents = new HashMap<>();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                String type = doc.getString("Type");
                if (!documents.containsKey(type)) {
                    documents.put(type,doc);
                }
            }
            for (Map.Entry<String, Document> entry : documents.entrySet()) {
                if (generateClass(entry.getKey(), entry.getValue())) {
                    System.out.println("Generated class " + entry.getKey());
                } else {
                    System.out.println("Skipped class " + entry.getKey());
                }
            }

            classes.forEach((k, v) -> {System.out.println("Class " + k + " : " + v.getName());});
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Boolean generateClass(String className, Document doc) throws Exception {
        //String className = doc.getString("Type");
        if(classes.containsKey(className)) return false;

        ClassPool pool = ClassPool.getDefault();
        CtClass superClass = pool.get(BASE_PACKAGE + ".Item");
        CtClass itemClass = pool.makeClass(BASE_PACKAGE + "."+className);

        itemClass.setSuperclass(superClass);

        CtConstructor constructor = new CtConstructor(new CtClass[]{}, itemClass);

        constructor.setBody("{ super(); }");
        itemClass.addConstructor(constructor);

        for(Map.Entry<String, Object> entry : doc.entrySet()) {
            String key = entry.getKey();
            if (key.equals("_id") || key.equals("Name") || key.equals("Type") || key.equals("Rarity") || key.equals("Description")) continue;
            Object value = entry.getValue();
            CtClass fieldType = inferCtClass(value);
            CtField field = new CtField(fieldType, key, itemClass);
            field.setModifiers(Modifier.PROTECTED);
            itemClass.addField(field);
            String methodSuffix = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            itemClass.addMethod(CtNewMethod.getter("get" + methodSuffix, field));
            itemClass.addMethod(CtNewMethod.setter("set" + methodSuffix, field));

        }

        DynamicClassLoader cl = new DynamicClassLoader();

        Class<?> clazz = cl.defineClass(itemClass.getName(), itemClass.toBytecode());
        classes.put(className, clazz);
        return true;
    }
    public static Map<String, Class<?>> getClasses() {
        return classes;
    }

    private static CtClass inferCtClass(Object value) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        if(value instanceof Integer) return CtClass.intType;
        if(value instanceof Double) return CtClass.doubleType;
        if(value instanceof Boolean) return CtClass.booleanType;
        return pool.get(String.class.getName());
    }
}
