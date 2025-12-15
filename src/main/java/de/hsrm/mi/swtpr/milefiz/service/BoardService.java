package de.hsrm.mi.swtpr.milefiz.service;

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

    public Board getBoardFromJson(String jsonFile) {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource(jsonFile);
        Board board;
        try {
            board = mapper.readValue(resource.getInputStream(), Board.class);
            logger.info("Get the board from the json file successfully");
        } catch (Exception e) {
            logger.info("Import from json file didnt work, getting the dummy");
            return dummyBoardService.createDummyBoard();
        }
        addStartFieldsToBoard(board);
        return board;
    }

    // unschoene Loesung, aber keine Ahnung wie ich das bei
    // dem vorhandenen Parser anders machen soll
    private void addStartFieldsToBoard(Board board) {
        for(int j = 0; j < board.getHeight(); j++) {
            for(int i = 0; i < board.getWidth(); i++) {
                Field field = board.get(i, j);
                if(field.getType().equals(CellType.START)) {
                    board.addStartField(field);
                    //logger.info("Startfeld {} gefunden an Pos {} | {}", board.getStartFields().size(), i, j);
                }
            }
        }

        for(int i = 0; i < board.getStartFields().size(); i++) {
            Field f = board.getStartFieldByIndex(i);
            logger.info("Gefundenes Startfeld {} : {} {}", i, f.getI(), f.getJ());
        }
    }
}
