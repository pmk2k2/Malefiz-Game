package de.hsrm.mi.swtpr.milefiz.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import ch.qos.logback.core.spi.ConfigurationEvent.EventType;
import de.hsrm.mi.swtpr.milefiz.model.DiceResult;

import de.hsrm.mi.swtpr.milefiz.exception.CooldownException;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.IngameMoveEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.IngameRequestEvent;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Nachrichtentyp;
import de.hsrm.mi.swtpr.milefiz.messaging.FrontendNachrichtEvent.Operation;
import de.hsrm.mi.swtpr.milefiz.messaging.IngameRequestEvent.Aktion;

@Service
public class DiceService {

    private ApplicationEventPublisher publisher;

    public DiceService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    private Random random = new Random();

    // cooldown für's Würfeln (3000 ms = 3.000.000.000 ns) standardwert
    private long cooldown = 3000L * 1000000;

    // cooldown kann damit eingestellt werden
    public void setCooldown(long seconds) {
        this.cooldown = seconds * 1_000_000_000;
    }

    public long getCooldown() {
        return cooldown / 1_000_000_000;
    }

    // Spieler mit ihrem letzten Zeitpunkt des Wurfs speichern
    private Map<String, Long> playerCooldown = new HashMap<>();

    public DiceResult rollDice(String playerName, String playerId, String gameCode) throws CooldownException {

        // aktuelle Zeit speichern
        long now = System.nanoTime();

        // Überprüfen, ob Spieler schon mal geworfen hat
        if (playerCooldown.containsKey(playerName)) {
            // differenz vom letzten und aktuellen Zeitpunkt bestimmen
            long lastRoll = playerCooldown.get(playerName);
            long dif = now - lastRoll;

            // wenn cooldown noch nicht fertig ist, wird Exception geworfen
            if (cooldown > dif) {
                long rest = cooldown - dif;
                throw new CooldownException(
                        "Du musst noch " + rest / 1_000_000_000 + " Sekunden bis zum nächsten Wurf warten");
            }
        }

        // Zahl zwischen 1 und 6 wird erstellt und im Ergebnisobjekt gespeichert
        int result = random.nextInt(6) + 1;
        DiceResult diceResult = new DiceResult(result, playerName);

        publisher.publishEvent(new IngameRequestEvent(Aktion.DICE_ROLL, playerId, gameCode, result));

        // Wurfzeitpunkt wird gespeichert
        playerCooldown.put(playerName, diceResult.getTimeStamp());

        // Richtung vom Frontend anfragen um Bewegung einzuleiten
        var event = new IngameRequestEvent(Aktion.DIRECTION, playerId, gameCode);
        publisher.publishEvent(event);

        return diceResult;
    }

}
