package be.helha.poo3.serverpoo.models;

import java.time.Instant;

public class ChallengeRequestDTO {
    private String id;
    private int challengerId;
    private String challengerName;
    private int targetId;
    private String targetName;
    private String status;
    private Instant lastUpdate;

    public ChallengeRequestDTO(ChallengeRequest challengeRequest) {
        this.id = challengeRequest.getId().toString();
        this.challengerId = challengeRequest.getChallenger().getId();
        this.challengerName = challengeRequest.getChallenger().getNom();
        this.targetId = challengeRequest.getTarget().getId();
        this.targetName = challengeRequest.getTarget().getNom();
        this.status = challengeRequest.getStatus().toString();
        this.lastUpdate = challengeRequest.getLastUpdate();
    }

    public String getId() {
        return id;
    }

    public int getChallengerId() {
        return challengerId;
    }

    public String getChallengerName() {
        return challengerName;
    }

    public int getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getStatus() {
        return status;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }
}
