package hvac.simulation.rooms;

import com.sun.tools.javac.util.Pair;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;

public class RoomMap {
    private Hashtable<Room, LinkedList<Pair<RoomWall, Room>>> roomGraph
            = new Hashtable<>();
    public void AddRoom(Room room) {
        if(roomGraph.containsKey(room))
            throw new RuntimeException("Attempting to add room twice");
        roomGraph.put(room, new LinkedList<>());
    }
    public void LinkRooms(Room r1, Room r2, RoomWall wall) {
        if(!roomGraph.containsKey(r1))
            throw new RuntimeException("Room r1 is not present on map");
        if(!roomGraph.containsKey(r2))
            throw new RuntimeException("Room r2 is not present on map");
        for(Pair<RoomWall, Room> roomLink : roomGraph.get(r1)) {
            if(roomLink.snd.equals(r2))
                throw new RuntimeException("Rooms already linked");
        }
        roomGraph.get(r1).add(new Pair<RoomWall, Room>(wall, r2));
        roomGraph.get(r2).add(new Pair<RoomWall, Room>(wall, r1));
    }
    public Iterable<Pair<RoomWall, Room>> GetNeighbors(Room r) {
        if(!roomGraph.containsKey(r))
            throw new RuntimeException("Room r is not present on map");
        return roomGraph.get(r);
    }
}
