package de.hsrm.mi.swtpr.milefiz.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.hsrm.mi.swtpr.milefiz.entities.board.CellType;
import de.hsrm.mi.swtpr.milefiz.entities.board.Field;
import de.hsrm.mi.swtpr.milefiz.entities.game.Figure;
import de.hsrm.mi.swtpr.milefiz.entities.game.Game;
import de.hsrm.mi.swtpr.milefiz.model.Direction;

@Service
public class BoardNavigationService {

    public enum MoveType {
        STRAIGHT_NS,
        STRAIGHT_WE,
        CURVE_WS,
        CURVE_ES,
        CURVE_WN,
        CURVE_EN,
        T_CROSSING,
        CROSSING,
        DEADEND
    };

    // alle begehbaren Nachbarfelder
    public Map<String, Field> getWalkableNeighbors(Game game, Figure figure) {

        Map<String, Field> result = new HashMap<>();

        int i = figure.getGridI();
        int j = figure.getGridJ();

        // Maske: west, ost, norden, sueden
        int[][] mask = {
            {-1, 0}, // west
            { 1, 0}, // ost
            { 0, 1}, // norden
            { 0,-1}  // sueden 
        };

        String[] names = { "west", "east", "north", "south" };

        for (int k = 0; k < mask.length; k++) {
            int ni = i + mask[k][0];
            int nj = j + mask[k][1];

            // Prüfung: innerhalb des Spielfelds
            if (ni < 0 || nj < 0 || ni >= game.getBoard().getWidth() || nj >= game.getBoard().getHeight()) {
                continue;
            }

            Field f = game.getBoard().get(ni, nj);

            // BLOCKED ist nicht begehbar
            if (f.getType() == CellType.BLOCKED) {
                continue;
            }

            result.put(names[k], f);
        }

        return result;
    }

    // Ermittlung von Richtung, Kreuzung
    public MoveType classifyField(Game game, Figure figure) {

        Map<String, Field> n = getWalkableNeighbors(game, figure);

        boolean west = n.containsKey("west");
        boolean east = n.containsKey("east");
        boolean north = n.containsKey("north");
        boolean south = n.containsKey("south");

        int count = n.size();

        // Kreuzung wenn = 4 Richtungen, T-Kreuzung bei 3 Richtungen
        if (count >= 4) return MoveType.CROSSING;
        if (count == 3) return MoveType.T_CROSSING;

        // Gerade Strecke
        boolean geradeNordSued = north && south && !west && !east;
        if(geradeNordSued) return MoveType.STRAIGHT_NS;

        boolean geradeWestOst  = !north && !south && west && east;
        if(geradeWestOst) return MoveType.STRAIGHT_WE;

        // Kurven
        boolean curveWestSouth = !north && south && west && !east;
        if(curveWestSouth)  return MoveType.CURVE_WS;
        boolean curveWestNorth = north && !south && west && !east;
        if(curveWestNorth)  return MoveType.CURVE_WN;
        boolean curveEastNorth = north && !south && !west && east;
        if(curveEastNorth)  return MoveType.CURVE_EN;
        boolean curveEastSouth = !north && south && !west && east;
        if(curveEastSouth)  return MoveType.CURVE_ES;

        // Ansonsten -> Sackgasse
        return MoveType.DEADEND;
    }

    // Statt zu gucken, welche Richtungen man darf, einfach schauen aus welcher man kommt (also nicht darf)
    // in dem Fall ist lastDir die gelaufene Richtung, also darf man nicht in die entgegengesetzte
    public Direction getForbiddenDirection(Direction lastDir) {
        Direction dir = null;

        switch(lastDir) {
            case Direction.NORTH:
                dir = Direction.SOUTH;
                break;
            case Direction.SOUTH:
                dir = Direction.NORTH;
                break;
            case Direction.EAST:
                dir = Direction.WEST;
                break;
            case Direction.WEST:
                dir = Direction.EAST;
                break;
        }

        return dir;
    }

    // Bei Kurven die richtige Richtung erhalten
    public Direction getNewDirection(MoveType mT, Direction dir) {
        Direction newDir;
        
        // Erst gucken nach Typ von Kurve, dann aus welcher Richtung man kommt
        switch(mT) {
            case MoveType.CURVE_WS:
                newDir = (dir == Direction.EAST) ? Direction.SOUTH : Direction.WEST;
                break;
            case MoveType.CURVE_ES:
                newDir = (dir == Direction.WEST) ? Direction.SOUTH : Direction.EAST;
                break;
            case MoveType.CURVE_WN:
                newDir = (dir == Direction.EAST) ? Direction.NORTH : Direction.WEST;
                break;
            default:
            case MoveType.CURVE_EN:
                newDir = (dir == Direction.WEST) ? Direction.NORTH : Direction.EAST;
                break;
        }

        return newDir;
    }
}