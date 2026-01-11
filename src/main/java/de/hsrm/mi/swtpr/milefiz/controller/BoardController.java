package de.hsrm.mi.swtpr.milefiz.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import de.hsrm.mi.swtpr.milefiz.entities.board.Board;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.service.GameService;
import de.hsrm.mi.swtpr.milefiz.service.BoardService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BoardController {
    @Autowired
    private GameService gameService;

    @Autowired
    private BoardService boardService;

    private Logger logger = LoggerFactory.getLogger(BoardController.class);

    @PostMapping("/board")
    public void receiveBoard(@RequestBody Board board, @RequestParam("code") String code) {
        if (board.getGrid() != null) {
            logger.info("BACKEND HAT DAS SPIELFELD GENOMMEN");
            Game theGame = gameService.getGame(code);
            theGame.setBoard(board);
        } else {
            logger.info("NICHTS GENOMMEN");
        }
    }

    @GetMapping("/board")
    public Board sendBoard(@RequestParam("code") String code) {
        try {
            Game theGame = gameService.getGame(code);
            Board b = theGame.getBoard();
            if (b.getCols() == 0) {
                return null;
            }
            logger.info("REST-Anfrage Board: Feld (5,1) ist Typ: {}", b.get(5, 1).getType());
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/lobby/{code}/boards/presets")
    public ResponseEntity<List<String>> getPresetBoards(@PathVariable String code) {
        Game game = gameService.getGame(code);
        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        List<String> presets = Arrays.asList(
                "DummyBoard.json",
                "BiggerBoard.json",
                "SmallerBoard.json",
                "MuchBigger.json");
        return ResponseEntity.ok(presets);
    }

    @GetMapping("/lobby/{code}/boards/preset/{presetName}")
    public ResponseEntity<Board> getPresetBoard(
            @PathVariable String code,
            @PathVariable String presetName) {

        Game game = gameService.getGame(code);
        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            Board board = boardService.getBoardFromJson(presetName);
            return ResponseEntity.ok(board);
        } catch (Exception e) {
            logger.error("Failed to load preset board: {}", presetName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/lobby/{code}/board")
    public ResponseEntity<Map<String, Object>> saveCustomBoard(
            @PathVariable String code,
            @RequestParam String playerId,
            @RequestBody Board customBoard) {

        Game game = gameService.getGame(code);
        Map<String, Object> response = new HashMap<>();

        if (game == null) {
            response.put("success", false);
            response.put("error", "Game not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Check if player is host
        var player = game.getPlayerById(playerId);
        if (player == null || !player.isHost()) {
            response.put("success", false);
            response.put("error", "Only host can modify board");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // Validate board basic structure
        if (customBoard.getGrid() == null || customBoard.getGrid().length == 0) {
            response.put("success", false);
            response.put("error", "Invalid board: grid is empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Save board to game
        game.setBoard(customBoard);
        logger.info("Custom board saved for game {}: {}x{}", code, customBoard.getCols(), customBoard.getRows());

        response.put("success", true);
        response.put("message", "Board saved successfully");
        return ResponseEntity.ok(response);
    }

    // NEW: Select a preset board for the lobby
    @PostMapping("/lobby/{code}/board/select-preset")
    public ResponseEntity<Map<String, Object>> selectPresetBoard(
            @PathVariable String code,
            @RequestParam String playerId,
            @RequestParam String presetName) {

        Game game = gameService.getGame(code);
        Map<String, Object> response = new HashMap<>();

        if (game == null) {
            response.put("success", false);
            response.put("error", "Game not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // Check if player is host
        var player = game.getPlayerById(playerId);
        if (player == null || !player.isHost()) {
            response.put("success", false);
            response.put("error", "Only host can select board");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            Board board = boardService.getBoardFromJson(presetName);
            game.setBoard(board);
            logger.info("Preset board '{}' selected for game {}", presetName, code);

            response.put("success", true);
            response.put("message", "Board selected successfully");
            response.put("boardName", presetName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to select preset board: {}", presetName, e);
            response.put("success", false);
            response.put("error", "Failed to load preset board");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // NEW: Get current board for lobby preview
    @GetMapping("/lobby/{code}/board")
    public ResponseEntity<Board> getLobbyBoard(@PathVariable String code) {
        Game game = gameService.getGame(code);

        if (game == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Board board = game.getBoard();
        if (board == null || board.getCols() == 0) {
            // Return default board if none set
            board = boardService.getBoardFromJson("DummyBoard.json");
        }

        return ResponseEntity.ok(board);
    }
}