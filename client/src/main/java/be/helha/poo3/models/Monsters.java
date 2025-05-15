package be.helha.poo3.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Monsters {


    slime("Slime", 20, 10, 10, Rarity.common,Temperament.passive),
    skeleton("Skeleton", 40, 25, 20, Rarity.uncommon,Temperament.aggressive),
    zombie("Zombie", 50, 20, 25, Rarity.uncommon,Temperament.aggressive),
    goblin("Goblin", 60, 35, 20, Rarity.rare,Temperament.neutral),
    orc("Orc", 100, 50, 50, Rarity.epic,Temperament.neutral),
    undeadKnight("Undead Knight", 150, 70,100, Rarity.legendary,Temperament.aggressive);


    private final String name;
    private final int health;
    private final int damage;
    private final int defense;
    private final Rarity rarity;
    private Temperament temperament = Temperament.neutral;

    Monsters(String name, int health, int damage, int defense, Rarity rarity, Temperament temperament) {
        this.name = name;
        this.health = health;
        this.damage = damage;
        this.defense = defense;
        this.rarity = rarity;
        this.temperament = temperament;
    }

    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getDamage() { return damage; }
    public int getDefense() { return defense; }
    public Rarity getRarity() { return rarity; }
    public Temperament getTemperament() { return temperament; }


    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static Monsters fromValue(String value) {
        return Monsters.valueOf(value);
    }
}

