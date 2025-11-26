package de.hsrm.mi.swtpr.milefiz.entities.player;

public class Player {
    private String name;
    private String id;
    private boolean isHost;
    private boolean isReady;

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public Player(String name, String id, boolean isHost, boolean isReady) {
        this.name = name;
        this.id = id;
        this.isHost = isHost;
        this.isReady= isReady;
    }

}
