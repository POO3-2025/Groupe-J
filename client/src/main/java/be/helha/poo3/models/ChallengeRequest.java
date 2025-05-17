package be.helha.poo3.models;


import java.time.Instant;

public class ChallengeRequest {
    private String id;
    private int challengerId;
    private String challengerName;
    private int targetId;
    private String targetName;
    private String status;
    private Instant lastUpdate;

    public ChallengeRequest(String id, int challengerId, String challengerName, int targetId, String targetName, String status, Instant lastUpdate) {
        this.id = id;
        this.challengerId = challengerId;
        this.challengerName = challengerName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.status = status;
        this.lastUpdate = lastUpdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(int challengerId) {
        this.challengerId = challengerId;
    }

    public String getChallengerName() {
        return challengerName;
    }

    public void setChallengerName(String challengerName) {
        this.challengerName = challengerName;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}

