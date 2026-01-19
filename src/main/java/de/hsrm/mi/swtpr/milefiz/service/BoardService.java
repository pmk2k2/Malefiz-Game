package de.hsrm.mi.swtpr.milefiz.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hsrm.mi.swtpr.milefiz.entities.board.Board;
import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;

@Service
public class BoardService {
    private Logger logger = LoggerFactory.getLogger(BoardService.class);

    @Autowired
    public DummyBoardService dummyBoardService;

    // Da ClassPathResource ein schreibgeschützter Bereich ist, nutzen wir diese
    // Methode um importierte Boards lokal zu speichern
    public void saveImportedBoard(String gameCode, String fileName, String content) throws IOException {
        Path uploadPath = Paths.get("imported_boards");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        logger.info("Board-Files successfully saved under {}", filePath.toAbsolutePath());
    }

    // Aktualisiert, damit die Methode zuerst vom neuen Ordner die
    // gespeicherte/importierte Boards holt
    public Board getBoardFromJson(String jsonFile) {
        ObjectMapper mapper = new ObjectMapper();

        Board board = null;
        try {
            Path externalPath = Paths.get("imported_boards").resolve(jsonFile);
            if (Files.exists(externalPath)) {
                board = mapper.readValue(externalPath.toFile(), Board.class);
                logger.info("Board loaded from external memory {}", jsonFile);
            } else {
                ClassPathResource resource = new ClassPathResource(jsonFile);
                board = mapper.readValue(resource.getInputStream(), Board.class);
                logger.info("Board loaded from ressources: {}", jsonFile);
            }

        } catch (Exception e) {
            logger.info("Error loading {}: {}", jsonFile, e.getMessage());
            return dummyBoardService.createDummyBoard();
        }
        if (board != null) {
            addStartFieldsToBoard(board);
        }

        return board;
    }

    // Die Methode gibt alle verfügbare Boards zurück (Sowohl im lokalen Ordner, als
    // auch die, die hardcoded sind.)
    public List<String> getAllAvailableBoards() {
        List<String> allBoards = new ArrayList<>(List.of(
                "DummyBoard.json",
                "BiggerBoard.json",
                "SmallerBoard.json",
                "MuchBigger.json"));

        try {
            Path uploadPath = Paths.get("imported_boards");
            if (Files.exists(uploadPath)) {
                try (var stream = Files.list(uploadPath)) {
                    List<String> imported = stream
                            .filter(file -> !Files.isDirectory(file))
                            .map(file -> file.getFileName().toString())
                            .filter(name -> name.endsWith(".json"))
                            .toList();

                    for (String name : imported) {
                        if (!allBoards.contains(name)) {
                            allBoards.add(name);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed scanning the imported boards", e);
        }

        return allBoards;
    }

    // Die Methode iteriert durch das Board(importierte .json-Datei) und prüft,
    // ob 1 Zielfeld und 4 Startfelder gefunden sind
    public void validateBoard(String content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Board board = mapper.readValue(content, Board.class);

        boolean hasGoal = false;
        int startFieldCounter = 0;

        for (Field[] row : board.getGrid()) {
            for (Field field : row) {
                if (field.getType().equals(CellType.GOAL)) {
                    hasGoal = true;
                }
                if (field.getType().equals(CellType.START)) {
                    startFieldCounter++;
                }
            }
        }

        if (!hasGoal) {
            throw new Exception("Map is not correct structured: No Goal-Field (GOAL) found.");
        }

        if (startFieldCounter != 4) {
            throw new Exception(
                    "Map is not correct structured: No Start-Field (START) found. The file must have 4 Start-Fields");
        }

        logger.info("Validation successful: Goal found, {} Start-Fields found.", startFieldCounter);
    }

    // unschoene Loesung, aber keine Ahnung wie ich das bei
    // dem vorhandenen Parser anders machen soll
    public void addStartFieldsToBoard(Board board) {
        for (int j = 0; j < board.getHeight(); j++) {
            for (int i = 0; i < board.getWidth(); i++) {
                Field field = board.get(i, j);
                if (field.getType().equals(CellType.START)) {
                    board.addStartField(field);
                    // logger.info("Startfeld {} gefunden an Pos {} | {}",
                    // board.getStartFields().size(), i, j);
                }
            }
        }

        for (int i = 0; i < board.getStartFields().size(); i++) {
            Field f = board.getStartFieldByIndex(i);
            logger.info("Gefundenes Startfeld {} : {} {}", i, f.getI(), f.getJ());
        }
    }

    public void saveJSONFile(Board board) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Writing to a file
            mapper.writeValue(new File("CustomBoard.json"), board);
            logger.info("Board successfully saved! Where is it? IDK");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
