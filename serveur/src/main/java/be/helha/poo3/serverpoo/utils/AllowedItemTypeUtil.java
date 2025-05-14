package be.helha.poo3.serverpoo.utils;

import be.helha.poo3.serverpoo.models.AllowedItemList;
import be.helha.poo3.serverpoo.models.Item;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AllowedItemTypeUtil {
    private static List<String> allowedItemTypes = new ArrayList<String>();
    private static String FILE_PATH = "config/allowedItems.json";

    public static void getAllowedItemTypesFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        URL resource = AllowedItemTypeUtil.class.getClassLoader().getResource(FILE_PATH);

        if(resource == null) throw new IOException("Resource not found");

        File file = new File(resource.getFile());

        AllowedItemList items = mapper.readValue(file, AllowedItemList.class);

        if(items == null) throw new IOException("Invalid file");

        if(items.getAllowedItems() == null) throw new IOException("List is empty");

        allowedItemTypes = new ArrayList<>(items.getAllowedItems());

        System.out.println(allowedItemTypes);

    }

    public static List<String> getAllowedItemTypes() {
        return new ArrayList<>(allowedItemTypes);
    }

    public static boolean isAllowedForMain(Item item) {
        return item.getSubType() != null && item.getSubType().equalsIgnoreCase("Weapon");
    }

    public static boolean isAllowedForArmor(Item item) {
        return item.getSubType() != null && item.getSubType().equalsIgnoreCase("Armor");
    }

    public static boolean isAllowedForSecond(Item item) {
        return item.getSubType() != null && item.getSubType().equalsIgnoreCase("Secondary equipement");
    }

}
