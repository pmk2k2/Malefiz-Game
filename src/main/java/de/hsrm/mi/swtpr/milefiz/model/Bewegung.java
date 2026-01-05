package de.hsrm.mi.swtpr.milefiz.model;

public record Bewegung(int startX, int startZ, int endX, int endZ, Direction dir, int steps, int remainingSteps) {
};
// entweder endpositionen oder anzahl steps + direction nutzen