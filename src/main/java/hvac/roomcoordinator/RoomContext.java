package hvac.roomcoordinator;

import hvac.ontologies.meeting.Meeting;
import hvac.simulation.rooms.RoomWall;
import hvac.util.Conversions;
import hvac.util.Logger;
import jade.core.AID;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class RoomContext {
    private static final float defaultTemperature = 21+273;
    private final int myRoomId;
    private final AID coordinator;
    private AID myRoomUpkeeper;
    private final HashMap<AID, RoomWall> myNeighbours = new HashMap<>();
    private final PriorityQueue<Meeting> meetingsQueue = new PriorityQueue<>();
    private final Map<String, AbstractMap.SimpleEntry<Meeting, Map<AID, Float>>> neighboursForecastStatus = new HashMap<>();
    private final Logger logger = new Logger();
    private final boolean meetingRoom;

    RoomContext(int myRoomId, AID coordinator, boolean meetingRoom){
        this.myRoomId = myRoomId;
        this.coordinator = coordinator;
        this.meetingRoom = meetingRoom;
    }

    public int getMyRoomId() {
        return myRoomId;
    }

    public AID getCoordinator() {
        return coordinator;
    }

    public AID getMyRoomUpkeeper() {
        return myRoomUpkeeper;
    }

    public void setMyRoomUpkeeper(AID myRoomUpkeeper){
        this.myRoomUpkeeper = myRoomUpkeeper;
    }

    public HashMap<AID, RoomWall>  getMyNeighbours() {
        return myNeighbours;
    }

    public void addMyNeighbour(AID key, RoomWall value){
        myNeighbours.put(key, value);
    }

    public void addMeeting(Meeting meeting){
        meetingsQueue.add(meeting);
    }

    public boolean isMeetingRoom() {
        return meetingRoom;
    }

    public List<Meeting> getMeetingsStartingBefore(LocalDateTime date){
        List<Meeting> result = new ArrayList<>();
        for (Meeting meeting : meetingsQueue) {
            if (Conversions.toLocalDateTime(meeting.getStartDate()).isAfter(date))
                break;
            result.add(meeting);
        }
        return result;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isTimeSlotAvailable(Meeting meeting){
        return meetingsQueue.stream().noneMatch(meeting::isOverlapping);
    }

    public float estimateTemperatureForNeighbour(Meeting meeting){
        float estimatedTemperature = defaultTemperature;
        long timeBusy = 0;
        long meetingDuration = Duration.between(meeting.getLocalEndDate(), meeting.getLocalStartDate()).toMillis();
        for (Meeting plannedMeeting:meetingsQueue){
            long timeOverlapping = plannedMeeting.millisecondsOverlapping(meeting);
            if (timeOverlapping > 0) {
                timeBusy += timeOverlapping;
                estimatedTemperature += plannedMeeting.getTemperature() * (float) (timeOverlapping / meetingDuration);
            }
        }
        return estimatedTemperature  - defaultTemperature * (float) (timeBusy) / meetingDuration;
    }

    public void removeMeeting(String ID){
        for (Meeting meeting:meetingsQueue){
            if (meeting.getMeetingID().equals(ID)){
                meetingsQueue.remove(meeting);
                return;
            }
        }
    }
    public void setMeetings(List<Meeting> meetings) {
        this.meetingsQueue.clear();
        this.meetingsQueue.addAll(meetings);
    }

    public void newForecastEntry(Meeting meeting){
        neighboursForecastStatus.put(meeting.getMeetingID(), new AbstractMap.SimpleEntry<>(meeting, new HashMap<>()));
    }

    public void removeForecastEntry(String ID){
        neighboursForecastStatus.remove(ID);
    }

    public void addNeighbourForecast(String ID, AID neighbourAID, float temperature){
        neighboursForecastStatus.get(ID).getValue().put(neighbourAID, temperature);
    }

    public boolean isForecastCompleted(String ID){
        return myNeighbours.size() == neighboursForecastStatus.get(ID).getValue().size();
    }

    public float computeForecast(String ID){
        float forecastSum = 0;
        float factorSum = 0;
        for (Map.Entry<AID, Float> entry:neighboursForecastStatus.get(ID).getValue().entrySet()){
            float factor = myNeighbours.get(entry.getKey()).getArea()
                    * myNeighbours.get(entry.getKey()).getHeatTransferCoefficient();
            factorSum += factor;
            forecastSum += entry.getValue() * factor;
        }
        return forecastSum/factorSum;
    }
}
