package de.hsrm.mi.swtpr.milefiz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hsrm.mi.swtpr.milefiz.entities.board.Board;

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
        return board;
    }
}
