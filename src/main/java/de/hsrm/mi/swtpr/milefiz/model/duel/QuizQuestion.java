package de.hsrm.mi.swtpr.milefiz.model.duel;

import java.util.List;

public class QuizQuestion {
    private String id;
    private String text;
    private List<String> answers;
    private int correctIndex;

    public QuizQuestion(String id, String text, List<String> answers, int correctIndex) {
        this.id = id;
        this.text = text;
        this.answers = answers;
        this.correctIndex = correctIndex;
    }

    public String getId() { 
        return id; 
    }
    public String getText() {
        return text;
    }
    public List<String> getAnswers() {
        return answers;
    }
    public int getCorrectIndex() {
        return correctIndex;
    }
}

