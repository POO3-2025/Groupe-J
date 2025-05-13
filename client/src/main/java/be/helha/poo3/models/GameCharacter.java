package be.helha.poo3.models;

public class GameCharacter {
    private int idCharacter;
    private int idUser;
    private String name;
    private String inventoryId;
    private int maxHP;
    private int currentHP;
    private int constitution;
    private int dexterity;
    private int strength;

    public int getIdCharacter() {
        return idCharacter;
    }

    public void setIdCharacter(int idCharacter) {
        this.idCharacter = idCharacter;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
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

    public GameCharacter() {
    }

    public GameCharacter(int idCharacter, int idUser, String name, String inventoryId, int maxHP, int currentHP, int constitution, int dexterity, int strength) {
        this.idCharacter = idCharacter;
        this.idUser = idUser;
        this.name = name;
        this.inventoryId = inventoryId;
        this.maxHP = maxHP;
        this.currentHP = currentHP;
        this.constitution = constitution;
        this.dexterity = dexterity;
        this.strength = strength;
    }
}
