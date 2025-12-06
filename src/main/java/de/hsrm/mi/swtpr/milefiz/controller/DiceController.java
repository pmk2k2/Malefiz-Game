package de.hsrm.mi.swtpr.milefiz.controller;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hsrm.mi.swtpr.milefiz.exception.CooldownException;
import de.hsrm.mi.swtpr.milefiz.model.DiceResult;
import de.hsrm.mi.swtpr.milefiz.service.DiceService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/daten")
public class DiceController {
    
    private final DiceService diceService;

    //@Autowired
    public DiceController(DiceService diceService){
        this.diceService = diceService;
    }

    @GetMapping("/roll")
    public DiceResult roll(@RequestParam (name = "playerName", defaultValue = "Player 1") String playerName) throws CooldownException {
        return diceService.rollDice(playerName);
    }
    
    @PostMapping("/cooldown")
    public void setCooldown(@RequestParam("seconds") long seconds) {
        diceService.setCooldown(seconds);
    }

    @GetMapping("/cooldown")
    public long getCooldown() {
        return diceService.getCooldown();
    }

}
