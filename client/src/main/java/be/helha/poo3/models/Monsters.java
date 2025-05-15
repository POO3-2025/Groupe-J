package be.helha.poo3.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Monsters {

    slime("Slime", 10, 5, 2, Rarity.common,Temperament.neutral),
    skeleton("Skeleton", 15, 7, 4, Rarity.uncommon,Temperament.aggressive),
    zombie("Zombie", 20, 5, 4, Rarity.uncommon,Temperament.aggressive),
    goblin("Goblin", 30, 5, 1, Rarity.rare,Temperament.passive),
    orc("Orc", 40, 10, 5, Rarity.epic,Temperament.neutral);


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

