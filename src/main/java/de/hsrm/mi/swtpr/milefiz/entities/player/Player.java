package de.hsrm.mi.swtpr.milefiz.entities.player;

public class Player {
    private String name;
    private String id;
    private boolean isHost;

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

    public Player(String name, String id, boolean isHost) {
        this.name = name;
        this.id = id;
        this.isHost = isHost;
    }

}
