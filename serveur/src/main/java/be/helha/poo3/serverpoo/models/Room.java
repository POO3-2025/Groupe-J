package be.helha.poo3.serverpoo.models;

import java.util.EnumMap;

public class Room {
    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    private EnumMap<Direction, Room> exits;
    private Chest chest;
    private Monster monster;
    private String id;
    private int x, y;
    private boolean hasMonster;
    private boolean hasChest;


    public Room(boolean hasMonster, boolean hasChest, Chest chest, Monster monster){
        this.id = x + ":" + y;
        this.hasMonster = hasMonster;
        this.hasChest = hasChest;

        //s'il y a un coffre, on l'instancie
        if (hasChest) {
            this.chest = chest;
        } else this.chest = null;

        //s'il y a un monstre, on l'instancie
        if (hasMonster) {
            this.monster = monster;
        } else this.monster = null;

        this.exits = new EnumMap<>(Direction.class);
    }

    public String getId(){
        return id;
    }

    public Chest getChest(){
        return chest;
    }

    public void setChest(Chest chest){
        this.chest = chest;
    }

    public Monster getMonster(){
        return monster;
    }

    public void setMonster(Monster monster){
        this.monster = monster;
    }

    public void setExit(Direction direction, Room neighbor){
        exits.put(direction, neighbor);
    }

    public Room getExit(Direction direction){
        return exits.get(direction);
    }

    public EnumMap<Direction, Room> getExits(){
        return exits;
    }

    public boolean getHasChest(){
        return hasChest;
    }

    public void setHasChest(boolean hasChest){
        this.hasChest = hasChest;
    }

    public boolean getHasMonster(){
        return hasMonster;
    }

    public void setHasMonster(boolean hasMonster){
        this.hasMonster = hasMonster;
    }

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;

        this.id = x + ":" + y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public Item openChest(){
        return chest.open();
    }


    @Override
    public String toString(){
        return "Room{" +
                "id='" + id + '\'' +
                ",le coffre contient un(e) " + chest.getItem() +
                ", exits=" + exits +
                '}';
    }
}

