package de.hsrm.mi.swtpr.milefiz.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import de.hsrm.mi.swtpr.milefiz.service.GameService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private GameService gameService;

    // sessionId -> playerId
    private static final Map<String, String> sessionIdToPlayerId = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String playerId = null;
        // nimmt playerId von header (von client)
        if (headerAccessor.getNativeHeader("playerId") != null
                && !headerAccessor.getNativeHeader("playerId").isEmpty()) {
            playerId = headerAccessor.getNativeHeader("playerId").get(0);
            sessionIdToPlayerId.put(sessionId, playerId);
            logger.info("WebSocket connected, sessionId: {}, playerId: {}", sessionId, playerId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String playerId = sessionIdToPlayerId.remove(sessionId);
        logger.info("WebSocket disconnected, sessionId: {}, playerId: {}", sessionId, playerId);

        if (playerId == null)
            return;

        // Remove player from all games by playerId
        for (String gameCode : gameService.getAllGameCodes()) {
            boolean removed = gameService.removePlayer(gameCode, playerId);
            if (removed) {
                logger.info("Removed player {} from game {}", playerId, gameCode);
                break;
            }
        }
    }
}
