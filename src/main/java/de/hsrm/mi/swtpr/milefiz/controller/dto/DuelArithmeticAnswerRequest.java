package de.hsrm.mi.swtpr.milefiz.controller.dto;

public record DuelArithmeticAnswerRequest(
    String gameCode, 
    String playerId, 
    String answer 
) {}