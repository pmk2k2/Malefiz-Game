package de.hsrm.mi.swtpr.milefiz.model;

public record Bewegung ( int endX, int endZ, Direction dir, int steps ) {};
// entweder endpositionen oder anzahl steps + direction nutzen