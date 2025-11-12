package de.hsrm.mi.swtpr.milefiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hsrm.mi.swtpr.milefiz.model.DiceResult;
import de.hsrm.mi.swtpr.milefiz.service.DiceService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/daten")
public class DiceController {
    
    private final DiceService diceService;

    @Autowired
    public DiceController(DiceService diceService){
        this.diceService = diceService;
    }

    @GetMapping("/roll")
    public DiceResult roll(@RequestParam (defaultValue = "Player 1") String playerName) {
        return diceService.rollDice(playerName);
    }
    

}
