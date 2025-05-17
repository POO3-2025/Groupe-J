package be.helha.poo3.models;

public class PVPCharacter {

    int playerId;
    String playerName;
    int playerHp;
    int playerMaxHP;
    String playerAction;
    int playerDamageTaken;

    public PVPCharacter(int playerId, String playerName, int playerHp, int playerMaxHP, String playerAction, int playerDamageTaken) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.playerHp = playerHp;
        this.playerMaxHP = playerMaxHP;
        this.playerAction = playerAction;
        this.playerDamageTaken = playerDamageTaken;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getPlayerHp() {
        return playerHp;
    }

    public void setPlayerHp(int playerHp) {
        this.playerHp = playerHp;
    }

    public int getPlayerMaxHP() {
        return playerMaxHP;
    }

    public void setPlayerMaxHP(int playerMaxHP) {
        this.playerMaxHP = playerMaxHP;
    }

    public String getPlayerAction() {
        return playerAction;
    }

    public void setPlayerAction(String playerAction) {
        this.playerAction = playerAction;
    }

    public int getPlayerDamageTaken() {
        return playerDamageTaken;
    }

    public void setPlayerDamageTaken(int playerDamageTaken) {
        this.playerDamageTaken = playerDamageTaken;
    }
}
