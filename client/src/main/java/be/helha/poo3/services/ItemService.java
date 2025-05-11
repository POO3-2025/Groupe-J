package be.helha.poo3.services;

import be.helha.poo3.models.Item;
import be.helha.poo3.models.Rarity;
import be.helha.poo3.models.Config;
import be.helha.poo3.components.DynamicClassGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class ItemService {
    private static final List<Item> items = new ArrayList<>();
    private static final Map<Item, ObjectId> itemIds = new HashMap<>();
    private static String rawJsonContent;

    public static void initialize() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(Config.getBaseUrl() + "/items"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        rawJsonContent = response.body(); // ➡️ stockage brut
        initializeFromJsonString(rawJsonContent);
    }

    public static void initializeFromJsonFile(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        rawJsonContent = mapper.readTree(new File(filePath)).toString();
        initializeFromJsonString(rawJsonContent);
    }

    public static void initializeFromJsonString(String jsonContent) throws Exception {
        items.clear();
        itemIds.clear();
        DynamicClassGenerator.resetClasses();

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> itemList = mapper.readValue(jsonContent, List.class);

        Map<String, Map<String, Object>> uniqueTypes = new HashMap<>();

        for (Map<String, Object> itemData : itemList) {
            String type = (String) itemData.get("type");
            uniqueTypes.putIfAbsent(type, itemData);
        }

        for (Map.Entry<String, Map<String, Object>> entry : uniqueTypes.entrySet()) {
            DynamicClassGenerator.generateClass(entry.getKey(), entry.getValue());
        }

        for (Map<String, Object> itemData : itemList) {
            String type = (String) itemData.get("type");
            Class<?> clazz = DynamicClassGenerator.getClasses().get(type);

            Item item = (Item) clazz.getDeclaredConstructor().newInstance();
            item.setName((String) itemData.get("name"));
            item.setType((String) itemData.get("type"));
            item.setSubType((String) itemData.get("subType"));
            item.setRarity(Enum.valueOf(Rarity.class, (String) itemData.get("rarity")));
            item.setDescription((String) itemData.get("description"));

            for (Map.Entry<String, Object> entry : itemData.entrySet()) {
                String key = entry.getKey();
                if (key.equals("_id") || key.equals("name") || key.equals("type")
                        || key.equals("subType") || key.equals("rarity") || key.equals("description")) {
                    continue;
                }
                Object val = entry.getValue();
                if (val instanceof Number n) {                          // JSON -> nombre
                    item.setInt(key, n.intValue());
                } else if (val instanceof String s && !s.isBlank()) {   // JSON -> chaîne
                    item.setInt(key, Integer.parseInt(s));
                }
            }


            Object idObject = itemData.get("_id");
            ObjectId objectId = new ObjectId(idObject.toString());
            itemIds.put(item, objectId);
            item.setId(objectId);

            items.add(item);
        }
    }

    public static String getRawJsonContent() {
        return rawJsonContent;
    }

    public static List<Item> getAllItems() {
        return items;
    }

    public static Item getItemByName(String name) {
        return items.stream()
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static Item getItemById(String id) {
        try {
            ObjectId searchId = new ObjectId(id);
            return getItemById(searchId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Item getItemById(ObjectId id) {
        return itemIds.entrySet().stream()
                .filter(entry -> entry.getValue().equals(id))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
