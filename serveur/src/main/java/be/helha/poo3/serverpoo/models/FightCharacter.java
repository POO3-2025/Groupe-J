package be.helha.poo3.serverpoo.models;

public class FightCharacter {
    private int id;
    private String nom;
    private int maxHP;
    private int currentHP;
    private int constitution;
    private int dexterity;
    private int strength;
    private int damage;
    private int defense;
    private int agility;

    public FightCharacter(int id, String nom, int maxHP, int currentHP, int constitution, int dexterity, int strength, int damage, int defense, int agility) {
        this.id = id;
        this.nom = nom;
        this.maxHP = maxHP;
        this.currentHP = currentHP;
        this.constitution = constitution;
        this.dexterity = dexterity;
        this.strength = strength;
        this.damage = damage;
        this.defense = defense;
        this.agility = agility;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int currentHP) {
        this.currentHP = currentHP;
    }

    public int getConstitution() {
        return constitution;
    }

    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getAgility() {
        return agility;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }
}
