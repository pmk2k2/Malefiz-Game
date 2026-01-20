package de.hsrm.mi.swtpr.milefiz.model.duel;

import java.time.Instant;

public class DuelAnswer {

    private String playerId;
    private int answerIndex;
    private Instant timestamp;

    public DuelAnswer(String playerId, int answerIndex, Instant timestamp) {
        this.playerId = playerId;
        this.answerIndex = answerIndex;
        this.timestamp = timestamp;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getAnswerIndex() {
        return answerIndex;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
