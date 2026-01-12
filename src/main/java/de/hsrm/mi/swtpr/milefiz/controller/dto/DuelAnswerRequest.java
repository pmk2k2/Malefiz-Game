package de.hsrm.mi.swtpr.milefiz.controller.dto;

public record DuelAnswerRequest(
    String gameCode,
    String playerId,
    String questionId,
    int answerIndex
) {}
