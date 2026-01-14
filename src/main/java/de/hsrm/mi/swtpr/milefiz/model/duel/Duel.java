package de.hsrm.mi.swtpr.milefiz.model.duel;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Duel {

    private String gameCode;
    private String player1Id;
    private String player2Id;
    private QuizQuestion question;
    private Map<String, DuelAnswer> answers = new HashMap<>();
    private boolean finished = false;
    private Instant questionStartedAt;

    public Duel(String gameCode, String p1, String p2, QuizQuestion question) {
        this.gameCode = gameCode;
        this.player1Id = p1;
        this.player2Id = p2;
        this.question = question;
        this.questionStartedAt = Instant.now();
    }

    public QuizQuestion getQuestion() {
        return question;
    }

    public Map<String, DuelAnswer> getAnswers() {
        return answers;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finish() {
        this.finished = true;
    }

    public String getOpponent(String playerId) {
        if (playerId.equals(player1Id))
            return player2Id;
        return player1Id;
    }

    public Instant getQuestionStartedAt() {
        return questionStartedAt;
    }

    public void resetForNewQuestion(QuizQuestion q) {
        this.question = q;
        this.answers.clear();
        this.questionStartedAt = Instant.now();
    }

    public void nextQuestion(QuizQuestion newQuestion) {
        this.question = newQuestion;
        this.answers.clear();
        this.questionStartedAt = Instant.now();
    }

}
