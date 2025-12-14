package de.hsrm.mi.swtpr.milefiz.model;

public enum Direction {
    NORTH("north"),
    EAST("east"),
    SOUTH("south"),
    WEST("west");

    private String dir;

    private Direction(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }
}