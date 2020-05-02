package hvac.simulation;

import hvac.simulation.rooms.*;

import java.util.Hashtable;

public class SimulationContext {
    private RoomMap roomMap = new RoomMap();
    private Hashtable<Room, RoomClimate> climates = new Hashtable<>();
    private OutsideClimate outsideClimate = new OutsideClimate();

    public RoomMap getRoomMap() {
        return roomMap;
    }

    public Hashtable<Room, RoomClimate> getClimates() {
        return climates;
    }

    public OutsideClimate getOutsideClimate() {
        return outsideClimate;
    }
}
