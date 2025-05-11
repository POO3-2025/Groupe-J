package be.helha.poo3.serverpoo.models;

import java.awt.*;

public class CharacterWithPos extends GameCharacter {
    private Point position;

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public CharacterWithPos(int idCharacter, int idUser, String name, String inventoryId, int maxHP, int currentHP, int constitution, int dexterity, int strength, Point position) {
        super(idCharacter, idUser, name, inventoryId, maxHP, currentHP, constitution, dexterity, strength);
        this.position = position;
    }
}
