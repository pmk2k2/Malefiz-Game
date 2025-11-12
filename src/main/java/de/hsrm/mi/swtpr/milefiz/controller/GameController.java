package de.hsrm.mi.swtpr.milefiz.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import de.hsrm.mi.swtpr.milefiz.service.GameService;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);
    @Autowired
    private GameService service;

    @GetMapping("/")
    public String index() {
        // TODO: Wenn jemand von einer Spiel hier zurückkommt und deren Spiel verlassen,
        // dann Game.getPlayers().remove(Name des Spieler) oder etwas ähnliches
        // Und noch weiter andere Sache aber weiß ich noch nicht
        return "welcome";
    }

    @PostMapping("/create")
    public String createGame(@RequestParam String name, Model model, HttpSession session) {
        String code = service.createGame();
        String playerId = session.getId();
        service.addPlayer(code, playerId, name, true);

        session.setAttribute("gameCode", code);
        session.setAttribute("playerName", name);
        session.setAttribute("isHost", true);
        logger.info("Player {} with id: {} created a game with code: {}", name, playerId, code);

        return "redirect:/lobby";
    }

    @PostMapping("/join")
    public String joinGame(@RequestParam String name, @RequestParam String code, Model model, HttpSession session) {
        String playerId = session.getId();
        boolean success = service.addPlayer(code, playerId, name, false);

        if (!success) {
            model.addAttribute("error", "Invalid game code or player with the same Id already existed");
            logger.info("Invalid game code or player with the same Id already existed");
            return "welcome";
        }

        session.setAttribute("gameCode", code);
        session.setAttribute("playerName", name);
        session.setAttribute("isHost", false);
        logger.info("Player {} with id: {} joined a game with code: {}", name, playerId, code);

        return "redirect:/lobby";
    }

    @GetMapping("/lobby")
    public String lobby(Model model, HttpSession session) {
        String code = (String) session.getAttribute("gameCode");
        String name = (String) session.getAttribute("playerName");

        if (code == null || name == null) {
            return "redirect:/";
        }

        model.addAttribute("gameCode", code);
        model.addAttribute("playerName", name);
        model.addAttribute("game", service.getGame(code));
        model.addAttribute("playerList", service.getGame(code).getPlayers());
        return "game";
    }
}
