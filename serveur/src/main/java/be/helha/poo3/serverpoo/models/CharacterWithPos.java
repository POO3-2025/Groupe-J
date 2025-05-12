package be.helha.poo3.serverpoo.models;

import java.awt.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CharacterWithPos extends GameCharacter {
    private Point position;
    private Date lastAction;


    public CharacterWithPos(int idCharacter, int idUser, String name, String inventoryId, int maxHP, int currentHP, int constitution, int dexterity, int strength, Point position) {
        super(idCharacter, idUser, name, inventoryId, maxHP, currentHP, constitution, dexterity, strength);
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Date getLastAction() {
        return lastAction;
    }
    public void setLastAction() {
        this.lastAction = new Date();
    }

    public boolean hasActedRecently(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("minutes doit être > 0");
        }
        if (lastAction == null) return false;

        long diffMillis = System.currentTimeMillis() - lastAction.getTime();
        return diffMillis <= TimeUnit.MINUTES.toMillis(minutes);
    }
}
