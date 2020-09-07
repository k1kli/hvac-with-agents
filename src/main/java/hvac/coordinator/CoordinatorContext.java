package hvac.coordinator;

import hvac.util.Logger;
import jade.core.AID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoordinatorContext {
    private final Map<Integer, Set<AID>> rooms = new HashMap<>();
    private int maxSeats = 0;
    private final HashMap<String, Meeting> meetingsToAssign = new HashMap<>();
    private final HashMap<String, Meeting> assignedMeetings = new HashMap<>();
    private final Logger logger = new Logger();

    public void addRoom(int seats, AID roomAID){
        if (! rooms.containsKey(seats)){
            if (maxSeats < seats){
                maxSeats = seats;
            }
            rooms.put(seats, new HashSet<>());
        }
        rooms.getOrDefault(seats,null).add(roomAID);
    }

    public Set<AID> getRoomsByNSeats(int seats){
        for (;seats <= maxSeats;seats++) {
            if (rooms.containsKey(seats)) {
                return rooms.get(seats);
            }
        }
        return null;
    }

    public HashMap<String, Meeting> getMeetingsToAssign() {
        return meetingsToAssign;
    }

    public HashMap<String, Meeting> getAssignedMeetings() {
        return assignedMeetings;
    }

    public Logger getLogger() {
        return logger;
    }
}
