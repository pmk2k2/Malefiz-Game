package de.hsrm.mi.swtpr.milefiz.entities.board;

public interface Board {
    Field get(int i, int j);
    int getWidth();
    int getHeight();
}
