package be.helha.poo3.serverpoo.models;

import org.bson.types.ObjectId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class PVPFight {
    private ObjectId id = new ObjectId();
    private final FightCharacter playerOne;
    private final FightCharacter playerTwo;
    private final AtomicInteger turn = new AtomicInteger(0);
    private final Map<Integer, PVPTurn> turns = new ConcurrentHashMap<>();
    private volatile boolean finished = false;

    public PVPFight(FightCharacter playerTwo, FightCharacter playerOne) {
        this.playerTwo = playerTwo;
        this.playerOne = playerOne;
    }

    public synchronized void submitAction(int playerId, String action, BiFunction<String,String,PVPFightDTO> resolver){
        if (finished) throw new IllegalStateException("Fight finished");

        int currentTurn = turn.get();
        PVPTurn pvpTurn = turns.computeIfAbsent(currentTurn, k -> new PVPTurn());

        if(playerId == playerOne.getId() && pvpTurn.getPlayerOneAction() == null) pvpTurn.playerOneAction = action;
        else if(playerId == playerTwo.getId() && pvpTurn.getPlayerTwoAction() == null) pvpTurn.playerTwoAction = action;
        else throw new IllegalArgumentException("Action already set or bad player");

        if(pvpTurn.getPlayerOneAction() != null && pvpTurn.getPlayerTwoAction() != null) {
            pvpTurn.result = resolver.apply(pvpTurn.getPlayerOneAction(), pvpTurn.getPlayerTwoAction());
            finished = pvpTurn.result.endFight;
            turn.incrementAndGet();
        }
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public FightCharacter getPlayerOne() {
        return playerOne;
    }

    public FightCharacter getPlayerTwo() {
        return playerTwo;
    }

    public AtomicInteger getTurn() {
        return turn;
    }

    public Map<Integer, PVPTurn> getTurns() {
        return turns;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public static class PVPTurn {
        private String playerOneAction, playerTwoAction;
        private PVPFightDTO result;

        public String getPlayerOneAction() {
            return playerOneAction;
        }

        public String getPlayerTwoAction() {
            return playerTwoAction;
        }

        public PVPFightDTO getResult() {
            return result;
        }
    }

}
