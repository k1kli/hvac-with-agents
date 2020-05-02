package hvac.simulation;

import hvac.simulation.rooms.*;

import java.util.Hashtable;

public class SimulationContext {
    public RoomMap getRoomMap() {
        return roomMap;
    }

    public Hashtable<Room, RoomClimate> getClimates() {
        return climates;
    }

    private RoomMap roomMap = new RoomMap();
    private Hashtable<Room, RoomClimate> climates = new Hashtable<>();
}
