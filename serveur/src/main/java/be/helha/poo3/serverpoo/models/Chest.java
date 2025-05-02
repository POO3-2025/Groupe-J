package be.helha.poo3.serverpoo.models;

public class Chest {
    private Item item;

    public Chest(Item item) {
        this.item = item;
    }

    public Item open() {
        return item;
    }

    public Item getItem() {
        return item;
    }
}

