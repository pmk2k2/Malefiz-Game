package de.hsrm.mi.swtpr.milefiz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class StompWebMessageBrokerConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // SimpleBroker hört auf /topic
        config.enableSimpleBroker("/topic");
        // Präfix für Nachrichten vom Client an den Server (z. B. @MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket-Endpunkt, den Clients verbinden sollen
        registry.addEndpoint("/stompbroker").setAllowedOriginPatterns("*").withSockJS();
    }
}
