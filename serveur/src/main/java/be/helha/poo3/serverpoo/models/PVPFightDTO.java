package be.helha.poo3.serverpoo.models;

public class PVPFightDTO {
    boolean endFight;
    int turn;
    int playerID;
    int playerOneId;
    String playerOneName;
    int playerOneHp;
    int playerOneMaxHP;
    String playerOneAction;
    int playerOneDamageTaken;
    int playerTwoId;
    String playerTwoName;
    int playerTwoHp;
    int playerTwoMaxHP;
    String playerTwoAction;
    int playerTwoDamageTaken;

    public PVPFightDTO(boolean endFight, int turn, int playerId, int playerOneId, String playerOneName, int playerOneHp, int playerOneMaxHP, String playerOneAction, int playerOneDamageTaken, int playerTwoId, String playerTwoName, int playerTwoHp, int playerTwoMaxHP, String playerTwoAction, int playerTwoDamageTaken){
        this.endFight = endFight;
        this.turn = turn;
        this.playerID = playerId;
        this.playerOneId = playerOneId;
        this.playerOneName = playerOneName;
        this.playerOneHp = playerOneHp;
        this.playerOneMaxHP = playerOneMaxHP;
        this.playerOneAction = playerOneAction;
        this.playerOneDamageTaken = playerOneDamageTaken;
        this.playerTwoId = playerTwoId;
        this.playerTwoName = playerTwoName;
        this.playerTwoHp = playerTwoHp;
        this.playerTwoMaxHP = playerTwoMaxHP;
        this.playerTwoAction = playerTwoAction;
        this.playerTwoDamageTaken = playerTwoDamageTaken;
    }

    public boolean isEndFight() {
        return endFight;
    }

    public void setEndFight(boolean endFight) {
        this.endFight = endFight;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public int getPlayerOneId() {
        return playerOneId;
    }

    public void setPlayerOneId(int playerOneId) {
        this.playerOneId = playerOneId;
    }

    public String getPlayerOneName() {
        return playerOneName;
    }

    public void setPlayerOneName(String playerOneName) {
        this.playerOneName = playerOneName;
    }

    public int getPlayerOneHp() {
        return playerOneHp;
    }

    public void setPlayerOneHp(int playerOneHp) {
        this.playerOneHp = playerOneHp;
    }

    public int getPlayerOneMaxHP() {
        return playerOneMaxHP;
    }

    public void setPlayerOneMaxHP(int playerOneMaxHP) {
        this.playerOneMaxHP = playerOneMaxHP;
    }

    public String getPlayerOneAction() {
        return playerOneAction;
    }

    public void setPlayerOneAction(String playerOneAction) {
        this.playerOneAction = playerOneAction;
    }

    public int getPlayerOneDamageTaken() {
        return playerOneDamageTaken;
    }

    public void setPlayerOneDamageTaken(int playerOneDamageTaken) {
        this.playerOneDamageTaken = playerOneDamageTaken;
    }

    public int getPlayerTwoId() {
        return playerTwoId;
    }

    public void setPlayerTwoId(int playerTwoId) {
        this.playerTwoId = playerTwoId;
    }

    public String getPlayerTwoName() {
        return playerTwoName;
    }

    public void setPlayerTwoName(String playerTwoName) {
        this.playerTwoName = playerTwoName;
    }

    public int getPlayerTwoHp() {
        return playerTwoHp;
    }

    public void setPlayerTwoHp(int playerTwoHp) {
        this.playerTwoHp = playerTwoHp;
    }

    public int getPlayerTwoMaxHP() {
        return playerTwoMaxHP;
    }

    public void setPlayerTwoMaxHP(int playerTwoMaxHP) {
        this.playerTwoMaxHP = playerTwoMaxHP;
    }

    public String getPlayerTwoAction() {
        return playerTwoAction;
    }

    public void setPlayerTwoAction(String playerTwoAction) {
        this.playerTwoAction = playerTwoAction;
    }

    public int getPlayerTwoDamageTaken() {
        return playerTwoDamageTaken;
    }

    public void setPlayerTwoDamageTaken(int playerTwoDamageTaken) {
        this.playerTwoDamageTaken = playerTwoDamageTaken;
    }
}

