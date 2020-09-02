package hvac.simulation;

import hvac.simulation.rooms.RoomClimate;
import hvac.simulation.rooms.RoomMap;
import hvac.util.Logger;

import java.util.Hashtable;

public class SimulationContext {
    private final RoomMap roomMap = new RoomMap();
    private final Hashtable<Integer, RoomClimate> climates = new Hashtable<>();
    private final OutsideClimate outsideClimate = new OutsideClimate();
    private final Logger logger = new Logger();

    public RoomMap getRoomMap() {
        return roomMap;
    }

    public Hashtable<Integer, RoomClimate> getClimates() {
        return climates;
    }

    public OutsideClimate getOutsideClimate() {
        return outsideClimate;
    }

    public Logger getLogger() {
        return logger;
    }
}
