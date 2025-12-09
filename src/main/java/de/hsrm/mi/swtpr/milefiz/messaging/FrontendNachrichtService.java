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
        // logger.info("Sende STOMP-Event an {}: {} ", destination, ev);
        messagingTemplate.convertAndSend(destination, ev);
        // daten aus FrontEndNachrichtEvent werden übertragen, wenn event (JOINED,LEFT,
        // usw) getriggert werden.
    }
}