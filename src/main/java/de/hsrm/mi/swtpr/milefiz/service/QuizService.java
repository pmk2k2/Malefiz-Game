package de.hsrm.mi.swtpr.milefiz.service;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.model.duel.QuizQuestion;

@Service
public class QuizService {

    private final List<QuizQuestion> questions = List.of(
        new QuizQuestion(
            "q1",
            "Wie viele Fachbereiche hat die Hochschule RheinMain insgesamt?",
            List.of("3", "5", "7"),
            1
        ),
        new QuizQuestion(
            "q2",
            "Wer ist der Studienleiter von Medieninformatik?",
            List.of(
                "Prof. Dr. Berdux",
                "Prof. Dr. Weitz",
                "Prof. Dr. Ulges"
            ),
            0
        ),
        new QuizQuestion(
            "q3",
            "Wie heißt die Sekretärin Frau Gehl mit Vornamen?",
            List.of("Yvonne", "Anke", "Ricarda"),
            1
        ),
        new QuizQuestion(
            "q4",
            "In welchem Jahr wurde die HSRM gegründet?",
            List.of("1954", "1985", "1971"),
            0
        )
    );

    private final Random random = new Random();

    public QuizQuestion getRandomQuestion() {
        return questions.get(random.nextInt(questions.size()));
    }
}

