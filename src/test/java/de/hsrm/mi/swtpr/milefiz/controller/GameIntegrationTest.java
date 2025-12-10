package de.hsrm.mi.swtpr.milefiz.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GameIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("null")
    @Test
    void testGameWorkflow() throws Exception {
        // 1. Spiel erstellen (Host "Sam")
        Map<String, String> createBody = Map.of("name", "Sam");
        MvcResult createResult = mockMvc.perform(post("/api/game/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isOk())
                .andReturn();

        // GameCode und Host-Session-ID extrahieren
        MockHttpSession hostSession = (MockHttpSession) createResult.getRequest().getSession();
        String gameCode = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class).get("gameCode").toString();
        Map<?, ?> responseMap = objectMapper.readValue(createResult.getResponse().getContentAsString(), Map.class);
        String hostPlayerId = responseMap.get("playerId").toString();

        // 2. Spiel beitreten ("Reggy")
        Map<String, String> joinBody = Map.of("name", "Reggy", "code", gameCode);
        mockMvc.perform(post("/api/game/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players", hasSize(2))); // Prüft, ob 2 Spieler im Spiel sind

        // 3. Spiel starten (Host)
        // Body mit PlayerID, um die Host-Autorisierung zu erfüllen
        Map<String, String> startBody = Map.of("playerId", hostPlayerId);

        /*mockMvc.perform(post("/api/game/" + gameCode + "/start")
                .session(hostSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
        
        // 4. Figuren abrufen
        mockMvc.perform(get("/api/game/" + gameCode + "/figures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10))) // 2 Spieler * 5 Figuren = 10
                .andExpect(jsonPath("$[0].color", is("#ff0000"))) // Host/Spieler 1 Farbe prüfen
                .andExpect(jsonPath("$[5].color", is("#ffff00"))); // Spieler 2 Farbe prüfen (Index 5 ist die 1. Figur des 2. Spielers)*/
    }
}
