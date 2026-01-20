package de.hsrm.mi.swtpr.milefiz.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import de.hsrm.mi.swtpr.milefiz.entities.board.Board;
import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;

@Service
public class BoardValidationService {

    public List<String> validateBoard(Board board) {
        // LOGGING

        System.out.println("--- VALIDIERUNG GESTARTET ---");
        if (board != null) {
            System.out.println("Board Maße: " + board.getCols() + "x" + board.getRows());

            // Zeige das erste Feld oben links
            if (board.getGrid() != null && board.getRows() > 0 && board.getCols() > 0) {
                Field f = board.get(0, 0);
                System.out.println("Typ von Feld (0,0): " + (f != null ? f.getType() : "null"));
            }
        } else {
            System.out.println("Board ist NULL!");
        }

        // LOGGING

        List<String> errors = new ArrayList<>();

        if (board == null) {
            errors.add("Board ist null.");
            return errors;
        }

        if (board.getGrid() == null || board.getRows() == 0 || board.getCols() == 0) {
            errors.add("Das Grid ist leer oder hat ungültige Dimensionen.");
            return errors;
        }

        // 1. Größe prüfen
        if (board.getRows() < 5 || board.getCols() < 5) {
            errors.add("Das Spielfeld ist zu klein (Minimum 5x5).");
        }

        // 2. Start- und Zielfelder zählen
        int startCount = 0;
        int endCount = 0; // Falls ihr Zielfelder habt

        for (int y = 0; y < board.getRows(); y++) {
            for (int x = 0; x < board.getCols(); x++) {
                Field f = board.get(x, y);
                // Achtung: Prüfen ob Field null ist, falls Arrays lückenhaft gefüllt sind
                if (f != null && f.getType() == CellType.START) {
                    startCount++;
                }
                if (f != null && f.getType() == CellType.GOAL) {
                    endCount++;
                }
            }
        }

        if (startCount < 1) {
            errors.add("Es muss mindestens ein Startfeld vorhanden sein.");
        }

        if (endCount > 1) {
            errors.add("Es darf nur ein Zielfeld vorhanden sein.");
        }
        if (endCount < 1) {
            errors.add("Es muss ein Zielfeld vorhanden sein.");
        }

        // 3. Pfad-Prüfung (hier können später Algorithmen wie BFS/DFS rein)
        if (!isBoardConnected(board)) {
            errors.add("Das Spielfeld hat unerreichbare Inseln oder Sackgassen (Beispiel).");
        }

        return errors;
    }

    //
    // PFADFINDUNG
    //
    //
    private boolean isBoardConnected(Board board) {

        return true;
    }
}
