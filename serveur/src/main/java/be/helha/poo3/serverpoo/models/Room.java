package be.helha.poo3.serverpoo.models;

import java.util.EnumMap;

public class Room {
    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    private EnumMap<Direction, Room> exits;
    private Chest chest;
    private String id;
    private int x, y;


    public Room(boolean hasMonster, boolean hasChest , Chest chest) {
        this.id = x+":"+y;
        if (hasChest){
            this.chest = chest;
        }else this.chest = null;

        this.exits = new EnumMap<>(Direction.class);
    }

    public String getId() {
        return id;
    }

    public Chest getChest() {
        return chest;
    }

    public void setChest(Chest chest) {
        this.chest = chest;
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

    public Item openChest() {
        return chest.open();
    }


    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ",le coffre contient un(e) " + chest.getItem() +
                ", exits=" + exits +
                '}';
    }
}

