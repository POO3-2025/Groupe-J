package be.helha.poo3.serverpoo.models;

import java.util.EnumMap;

public class Room {
    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    private EnumMap<Direction, Room> exits;
    private boolean hasMonster;
    private boolean hasChest;
    private String id;
    private int x, y;

    public Room(boolean hasMonster, boolean hasChest) {
        this.id = x+":"+y;
        this.hasMonster = hasMonster;
        this.hasChest = hasChest;
        this.exits = new EnumMap<>(Direction.class);
    }

    public String getId() {
        return id;
    }

    public void setExit(Direction direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    public Room getExit(Direction direction) {
        return exits.get(direction);
    }

    public EnumMap<Direction, Room> getExits() {
        return exits;
    }

    public boolean hasMonster() {
        return hasMonster;
    }

    public boolean hasChest() {
        return hasChest;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;

        this.id = x+":"+y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", hasChest=" + hasChest +
                ", hasMonster=" + hasMonster +
                ", exits=" + exits +
                '}';
    }
}

