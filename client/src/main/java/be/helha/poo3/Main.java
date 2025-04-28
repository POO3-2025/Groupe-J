package be.helha.poo3;

import be.helha.poo3.models.Item;
import be.helha.poo3.services.ItemService;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            ItemService.initialize();

            System.out.println("--- Liste de tous les items ---");
            List<Item> items = ItemService.getAllItems();
            items.forEach(System.out::println);

            System.out.println("--- Tests de recherche ---");
            String name = "Small potion";
            Item itemByName = ItemService.getItemByName(name);
            if (itemByName != null) {
                System.out.println("Item trouvé par nom : " + itemByName);
            } else {
                System.out.println("Aucun item trouvé avec ce nom.");
            }

            Item itemById = ItemService.getItemById("6808b3630039800c4db5b41e");
            if (itemById != null) {
                System.out.println("Item trouvé par id : " + itemById);
            } else {
                System.out.println("Aucun item trouvé avec cet ID.");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du client : " + e.getMessage());
            e.printStackTrace();
        }
    }
}