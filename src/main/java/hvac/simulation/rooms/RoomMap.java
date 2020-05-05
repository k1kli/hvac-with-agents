package hvac.simulation.rooms;

import java.util.Hashtable;
import java.util.LinkedList;

public class RoomMap {
    private Hashtable<Room, LinkedList<RoomLink>> roomGraph
            = new Hashtable<>();

    public void addRoom(Room room) {
        if(roomGraph.containsKey(room))
            throw new RuntimeException("Attempting to add room twice");
        roomGraph.put(room, new LinkedList<>());
    }

    public void linkRooms(Room r1, Room r2, RoomWall wall) {
        if(!roomGraph.containsKey(r1))
            throw new RuntimeException("Room r1 is not present on map");
        if(!roomGraph.containsKey(r2))
            throw new RuntimeException("Room r2 is not present on map");
        for(RoomLink roomLink : roomGraph.get(r1)) {
            if(roomLink.getNeighbor().equals(r2))
                throw new RuntimeException("Rooms already linked");
        }
        roomGraph.get(r1).add(new RoomLink(r2, wall));
        roomGraph.get(r2).add(new RoomLink(r1, wall));
    }

    public Iterable<RoomLink> getNeighbors(Room r) {
        if(!roomGraph.containsKey(r))
            throw new RuntimeException("Room r is not present on map");
        return roomGraph.get(r);
    }

    public Iterable<Room> getRooms() {
        return roomGraph.keySet();
    }
}
