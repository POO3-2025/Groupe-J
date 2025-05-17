package be.helha.poo3.models;

public class PvmTurnResult {
    private boolean fightEnd;
    private String player;
    private String monster;
    private int playerHealth;
    private int monsterHealth;
    private int playerMaxHealth;
    private int monsterMaxHealth;
    private int damageMonsterTake;
    private int damagePlayerTake;
    private String playerAction;
    private String monsterAction;

    public PvmTurnResult(boolean fightEnd, String player, String monster, int playerHealth, int monsterHealth, int playerMaxHealth, int monsterMaxHealth, int damageMonsterTake, int damagePlayerTake, String playerAction, String monsterAction) {
        this.fightEnd = fightEnd;
        this.player = player;
        this.monster = monster;
        this.playerHealth = playerHealth;
        this.monsterHealth = monsterHealth;
        this.playerMaxHealth = playerMaxHealth;
        this.monsterMaxHealth = monsterMaxHealth;
        this.damageMonsterTake = damageMonsterTake;
        this.damagePlayerTake = damagePlayerTake;
        this.playerAction = playerAction;
        this.monsterAction = monsterAction;
    }

    public boolean isFightEnd() {
        return fightEnd;
    }

    public void setFightEnd(boolean fightEnd) {
        this.fightEnd = fightEnd;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getMonster() {
        return monster;
    }

    public void setMonster(String monster) {
        this.monster = monster;
    }

    public int getPlayerHealth() {
        return playerHealth;
    }

    public void setPlayerHealth(int playerHealth) {
        this.playerHealth = playerHealth;
    }

    public int getMonsterHealth() {
        return monsterHealth;
    }

    public void setMonsterHealth(int monsterHealth) {
        this.monsterHealth = monsterHealth;
    }

    public int getPlayerMaxHealth() {
        return playerMaxHealth;
    }

    public void setPlayerMaxHealth(int playerMaxHealth) {
        this.playerMaxHealth = playerMaxHealth;
    }

    public int getMonsterMaxHealth() {
        return monsterMaxHealth;
    }

    public void setMonsterMaxHealth(int monsterMaxHealth) {
        this.monsterMaxHealth = monsterMaxHealth;
    }

    public int getDamageMonsterTake() {
        return damageMonsterTake;
    }

    public void setDamageMonsterTake(int damageMonsterTake) {
        this.damageMonsterTake = damageMonsterTake;
    }

    public int getDamagePlayerTake() {
        return damagePlayerTake;
    }

    public void setDamagePlayerTake(int damagePlayerTake) {
        this.damagePlayerTake = damagePlayerTake;
    }

    public String getPlayerAction() {
        return playerAction;
    }

    public void setPlayerAction(String playerAction) {
        this.playerAction = playerAction;
    }

    public String getMonsterAction() {
        return monsterAction;
    }

    public void setMonsterAction(String monsterAction) {
        this.monsterAction = monsterAction;
    }
}