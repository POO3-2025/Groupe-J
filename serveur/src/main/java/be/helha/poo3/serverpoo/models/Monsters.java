package be.helha.poo3.serverpoo.models;

public enum Monsters {

    slime("Slime", 20, 10, 10, Rarity.common),
    skeleton("Skeleton", 40, 25, 20, Rarity.uncommon),
    zombie("Zombie", 50, 20, 25, Rarity.uncommon),
    goblin("Goblin", 60, 35, 20, Rarity.rare),
    orc("Orc", 100, 50, 50, Rarity.epic),
    undeadKnight("Undead Knight", 150, 70,100, Rarity.legendary);



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
