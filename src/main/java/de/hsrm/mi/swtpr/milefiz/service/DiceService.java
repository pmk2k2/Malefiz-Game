package de.hsrm.mi.swtpr.milefiz.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.model.DiceResult;


@Service
public class DiceService {

    Random random = new Random();

    public DiceResult rollDice(String playerName){

        int result = random.nextInt(6) +1;
        return new DiceResult(result, playerName);
    }
    
}
