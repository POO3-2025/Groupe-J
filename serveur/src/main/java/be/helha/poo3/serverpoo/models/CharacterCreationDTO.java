package be.helha.poo3.serverpoo.models;

public class CharacterCreationDTO {
    String name;
    int constitution;
    int dexterity;
    int strength;
    String classe;

    public CharacterCreationDTO(String name, int constitution, int dexterity, int strength, String classe) {
        this.name = name;
        this.constitution = constitution;
        this.dexterity = dexterity;
        this.strength = strength;
        this.classe = classe;
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
    public String getClasse() {return classe;}
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
    public void setClasse(String classe) {}
}

