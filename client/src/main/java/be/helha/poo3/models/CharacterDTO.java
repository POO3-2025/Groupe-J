package be.helha.poo3.models;

public class CharacterDTO {
    String name;
    int constitution;
    int dexterity;
    int strength;

    public CharacterDTO(String name, int constitution, int dexterity, int strength) {
        this.name = name;
        this.constitution = constitution;
        this.dexterity = dexterity;
        this.strength = strength;
    }
    public String getName() {
        return name;
    }
    public int getConstitution() {
        return constitution;
    }
    public int getDexterity() {
        return dexterity;
    }
    public int getStrength() {
        return strength;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }
    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }
    public void setStrength(int strength) {
        this.strength = strength;
    }
}
