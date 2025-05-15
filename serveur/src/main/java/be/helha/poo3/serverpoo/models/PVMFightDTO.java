package be.helha.poo3.serverpoo.models;

public class PVMFightDTO {
    boolean endFight;
    int monsterHp;
    int monsterMaxHp;
    int playerHp;
    int playerMaxHp;
    String monsterName;
    String monsterAction;
    String playerName;
    String playerAction;

    public PVMFightDTO(boolean endfight, int monsterHp, int monsterMaxHp, int playerHp, int playerMaxHp, String monsterName, String monsterAction, String playerName, String playerAction) {
        this.endFight = endFight;
        this.monsterHp = monsterHp;
        this.monsterMaxHp = monsterMaxHp;
        this.playerHp = playerHp;
        this.playerMaxHp = playerMaxHp;
        this.monsterName = monsterName;
        this.monsterAction = monsterAction;
        this.playerName = playerName;
        this.playerAction = playerAction;
    }

    public boolean isEndFight() {
        return endFight;
    }

    public int getMonsterHp() {
        return monsterHp;
    }

    public void setMonsterHp(int monsterHp) {
        this.monsterHp = monsterHp;
    }

    public int getMonsterMaxHp() {
        return monsterMaxHp;
    }

    public void setMonsterMaxHp(int monsterMaxHp) {
        this.monsterMaxHp = monsterMaxHp;
    }

    public int getPlayerHp() {
        return playerHp;
    }

    public void setPlayerHp(int playerHp) {
        this.playerHp = playerHp;
    }

    public int getPlayerMaxHp() {
        return playerMaxHp;
    }

    public void setPlayerMaxHp(int playerMaxHp) {
        this.playerMaxHp = playerMaxHp;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    public String getMonsterAction() {
        return monsterAction;
    }

    public void setMonsterAction(String monsterAction) {
        this.monsterAction = monsterAction;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerAction() {
        return playerAction;
    }

    public void setPlayerAction(String playerAction) {
        this.playerAction = playerAction;
    }
}
