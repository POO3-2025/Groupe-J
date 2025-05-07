package be.helha.poo3.serverpoo.models;

public enum Monsters {

    slime("Slime", 10, 5, 2, Rarity.common),
    skeleton("Skeleton", 15, 7, 4, Rarity.uncommon),
    zombie("Zombie", 20, 5, 4, Rarity.uncommon),
    goblin("Goblin", 30, 5, 1, Rarity.rare),
    orc("Orc", 40, 10, 5, Rarity.epic);



    private final String name;
    private final int health;
    private final int damage;
    private final int defense;
    private final Rarity rarity;

    Monsters(String name, int health, int damage, int defense, Rarity rarity) {
        this.name = name;
        this.health = health;
        this.damage = damage;
        this.defense = defense;
        this.rarity = rarity;
    }

    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getDamage() { return damage; }
    public int getDefense() { return defense; }
    public Rarity getRarity() { return rarity; }
}
