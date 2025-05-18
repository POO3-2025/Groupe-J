package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Item;
import be.helha.poo3.serverpoo.services.ItemLoaderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ItemController {

    private final ItemLoaderService itemLoaderService;

    public ItemController(final ItemLoaderService itemLoaderService) {
        this.itemLoaderService = itemLoaderService;
    }

    @GetMapping("/items")
    public List<Map<String, Object>> getAllItems() {
        return itemLoaderService.getLoadedItems()
                .stream()
                .map(Item::getMap)
                .collect(Collectors.toList());
    }
}
