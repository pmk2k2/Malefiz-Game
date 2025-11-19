package de.hsrm.mi.swtpr.milefiz.model;

public class DiceResult {

    private int value;
    private String playerName;
    private long timeStamp;

    public DiceResult(int value, String playerName){
        this.value = value;
        this.playerName = playerName;
        this.timeStamp = System.nanoTime();;
    }

    public int getValue(){
        return value;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTimeStamp(){
        return timeStamp;
    }
}
