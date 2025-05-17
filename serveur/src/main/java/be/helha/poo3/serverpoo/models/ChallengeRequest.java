package be.helha.poo3.serverpoo.models;

import org.bson.types.ObjectId;

import java.time.Instant;

public class ChallengeRequest {
    private final ObjectId id = new ObjectId();
    private final FightCharacter challenger;
    private final FightCharacter target;
    private ChallengeStatus status = ChallengeStatus.PENDING;
    private Instant lastUpdate = Instant.now();

    public ChallengeRequest(FightCharacter chalenger, FightCharacter target) {
        this.challenger = chalenger;
        this.target = target;
    }

    public ObjectId getId() {
        return id;
    }

    public FightCharacter getChallenger() {
        return challenger;
    }

    public FightCharacter getTarget() {
        return target;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public enum ChallengeStatus { PENDING, ACCEPTED, DECLINED }

}
