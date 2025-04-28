package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.services.ItemLoaderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ItemController {

    private final ItemLoaderService itemLoaderService;

    public ItemController(final ItemLoaderService itemLoaderService) {
        this.itemLoaderService = itemLoaderService;
    }

    @GetMapping("/items")
    public List<Map<String, Object>> getAllItems() {
        List<Map<String, Object>> serializedItems = new ArrayList<>();

        for (Item item : itemLoaderService.getLoadedItems()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", item.getName());
            map.put("type", item.getType());
            map.put("subType", item.getSubType());
            map.put("rarity", item.getRarity());
            map.put("description", item.getDescription());

            if (item.getId() != null) {
                map.put("_id", item.getId().toHexString());
            }

            // Ajout des attributs dynamiques
            Field[] fields = item.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    String fieldName = field.getName();
                    if (!List.of("name", "type", "subType", "rarity", "description", "id").contains(fieldName)) {
                        Object value = field.get(item);
                        if (value != null) {
                            map.put(fieldName, value);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            serializedItems.add(map);
        }

        return serializedItems;
    }
}
