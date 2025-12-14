package de.hsrm.mi.swtpr.milefiz.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class FrontendNachrichtService {

    private static final Logger logger = LoggerFactory.getLogger(FrontendNachrichtService.class);
    private final SimpMessagingTemplate messagingTemplate;

    public FrontendNachrichtService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void sendEvent(FrontendNachrichtEvent ev) {
        String destination = "/topic/gameSession/" + ev.getGameCode();
        logger.info("Player-ID: {}, Player-Name: {}", ev.getId(), ev.getPlayerName());
        logger.info("Sende STOMP-Event an {}: {} ", destination, ev);
        messagingTemplate.convertAndSend(destination, ev);
        // daten aus FrontEndNachrichtEvent werden übertragen, wenn event (JOINED,LEFT,
        // usw) getriggert werden.
    }

    @EventListener
    public void sendRequestEventForPlayer(IngameRequestEvent ev) {
        String destination = String.format("/queue/gameSession/%s/%s", ev.getGameCode(), ev.getPlayerId());
        logger.info("REQUEST: STOMP-Event an {}: {}", destination, ev);
        messagingTemplate.convertAndSend(destination, ev);
    }

    /*
    @EventListener
    public void sendMoveEvent(IngameMoveEvent ev) {
        String destination = String.format("/topic/gameSession/%s", ev.getGameCode());
        logger.info("MOVE: STOMP-Event an {}: {}", destination, ev);
        messagingTemplate.convertAndSend(destination, ev);
    }
    */
}