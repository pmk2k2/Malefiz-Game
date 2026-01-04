package de.hsrm.mi.swtpr.milefiz.entities.player;

public class Player {

    private String name;
    private String id;
    private boolean isHost;
    private boolean isReady;
    private String color; // Spieler- / Figurenfarbe
    private int energy = 0;

    // Konstruktor aus Datei 1 (Lobby / Ready-State)
    public Player(String name, String id, boolean isHost, boolean isReady) {
        this.name = name;
        this.id = id;
        this.isHost = isHost;
        this.isReady = isReady;
    }

    // Konstruktor aus Datei 2 (Spiel & Farbe)
    public Player(String name, String id, boolean isHost, String color) {
        this.name = name;
        this.id = id;
        this.isHost = isHost;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public void addEnergy(int amount, int maxLimit) {
        this.energy = Math.min(this.energy + amount, maxLimit);
    }

    public void consumeEnergy(int amount) {
        this.energy = Math.max(0, this.energy - amount);
    }
}
