package de.hsrm.mi.swtpr.milefiz.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.model.duel.QuizQuestion;

@Service
public class ArithmetikService {

    private final Random random = new Random();

    
    // Generiert eine zufällige Mathe-Aufgabe und gibt sie als QuizQuestion zurück.
    public QuizQuestion generateQuestion() {
        int a = random.nextInt(100) + 1; // Zahl zwischen 1 und 100
        int b = random.nextInt(100) + 1;
        int operator = random.nextInt(3); // 0:+, 1:-, 2:*

        String text = "";
        int result = 0;

        switch (operator) {
            case 0:
                text = a + " + " + b;
                result = a + b;
                break;
            case 1:
                // Für keine negativen Zahlen 
                if (a < b) { int temp = a; a = b; b = temp; }
                text = a + " - " + b;
                result = a - b;
                break;
            case 2:
                // Bei Mal-Rechnen kleinere Zahlen nehmen, damit es nicht zu schwer wird
                a = random.nextInt(20) + 1;
                b = random.nextInt(20) + 1;
                text = a + " * " + b;
                result = a * b;
                break;
        }

        String questionText = "Berechne: " + text;

        // Antworten generieren 
        List<String> answers = new ArrayList<>();
        String correctAnswerStr = String.valueOf(result);
        answers.add(correctAnswerStr);

        // 3 falsche Antworten generieren
        while (answers.size() < 4) {
            // Zufällige Abweichung zwischen -5 und +5 (aber nicht 0)
            int offset = random.nextInt(11) - 5; 
            if (offset == 0) offset = 1;
            
            int wrongResult = result + offset;
            String wrongStr = String.valueOf(wrongResult);

            // Keine Duplikate zulassen
            if (!answers.contains(wrongStr) && wrongResult >= 0) {
                answers.add(wrongStr);
            }
        }

        // Antworten mischen, damit die richtige nicht immer an Stelle 0 steht
        Collections.shuffle(answers);

        // Den Index der richtigen Antwort finden (für den QuizQuestion Konstruktor)
        int correctIndex = answers.indexOf(correctAnswerStr);

        // Eine eindeutige ID generieren (z.B. "math_12345")
        String id = "math_" + UUID.randomUUID().toString().substring(0, 8);

        // Neues QuizQuestion Objekt zurückgeben
        return new QuizQuestion(id, questionText, answers, correctIndex);
    }
}