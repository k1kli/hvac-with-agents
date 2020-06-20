package hvac.coordinator;

import java.util.HashMap;

public class CoordinatorContext {
    private HashMap<String, Meeting> meetingsToAssign = new HashMap<>();
    private HashMap<String, Meeting> assignedMeetings = new HashMap<>();

    public HashMap<String, Meeting> getMeetingsToAssign() {
        return meetingsToAssign;
    }

    public HashMap<String, Meeting> getAssignedMeetings() {
        return assignedMeetings;
    }
}
