package be.helha.poo3.components;


import be.helha.poo3.models.Item;
import javassist.*;

import java.util.HashMap;
import java.util.Map;

public class DynamicClassGenerator {
    private static final Map<String, Class<?>> classes = new HashMap<>();
    private static final String BASE_PACKAGE = "client.generated";

    public static boolean generateClass(String className, Map<String, Object> attributes) throws Exception {
        if (classes.containsKey(className)) return false;

        ClassPool pool = ClassPool.getDefault();
        CtClass superClass = pool.get(Item.class.getName());
        CtClass itemClass = pool.makeClass(BASE_PACKAGE + "." + className);
        itemClass.setSuperclass(superClass);

        CtConstructor constructor = new CtConstructor(new CtClass[]{}, itemClass);
        constructor.setBody("{ super(); }");
        itemClass.addConstructor(constructor);

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            if (key.equals("_id") || key.equals("name") || key.equals("type")
                    || key.equals("subType") || key.equals("rarity") || key.equals("description")) {
                continue;
            }

            String fieldName = Character.toLowerCase(key.charAt(0)) + key.substring(1);
            Object value = entry.getValue();

            CtClass fieldType = inferCtClass(value);
            CtField field = new CtField(fieldType, fieldName, itemClass);
            field.setModifiers(Modifier.PROTECTED);
            itemClass.addField(field);

            String methodSuffix = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            itemClass.addMethod(CtNewMethod.getter("get" + methodSuffix, field));
            itemClass.addMethod(CtNewMethod.setter("set" + methodSuffix, field));
        }

        DynamicClassLoader cl = new DynamicClassLoader();
        Class<?> clazz = cl.defineClass(itemClass.getName(), itemClass.toBytecode());

        itemClass.detach();
        classes.put(className, clazz);

        return true;
    }

    public static Map<String, Class<?>> getClasses() {
        return classes;
    }

    public static void resetClasses() {
        classes.clear();
    }

    private static CtClass inferCtClass(Object value) throws NotFoundException {
        ClassPool pool = ClassPool.getDefault();
        if (value instanceof Integer) return CtClass.intType;
        if (value instanceof Double) return CtClass.doubleType;
        if (value instanceof Boolean) return CtClass.booleanType;
        return pool.get(String.class.getName());
    }
}
