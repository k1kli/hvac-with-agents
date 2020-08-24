package hvac.roomcoordinator;

import hvac.ontologies.meeting.Meeting;
import hvac.simulation.rooms.RoomWall;
import jade.core.AID;

import java.util.*;

public class RoomContext {
    private static final float defaultTemperature = 21;
    private AID coordinator;
    private AID myRoomUpkeeper;
    private HashMap<AID, RoomWall> myNeighbours;
    private Meeting currentMeeting;
    private PriorityQueue<Meeting> meetingsQueue = new PriorityQueue<>();
    private Map<String, AbstractMap.SimpleEntry<Meeting, Map<AID, Float>>> neighboursForecastStatus = new HashMap<>();

    RoomContext(AID myRoomUpkeeper, AID coordinator, HashMap<AID, RoomWall>  myNeighbours){
        this.coordinator = coordinator;
        this.myRoomUpkeeper = myRoomUpkeeper;
        this.myNeighbours = myNeighbours;
    }

    public AID getCoordinator() {
        return coordinator;
    }

    @SuppressWarnings("unused")
    public AID getMyRoomUpkeeper() {
        return myRoomUpkeeper;
    }

    public HashMap<AID, RoomWall>  getMyNeighbours() {
        return myNeighbours;
    }

    public Meeting getCurrentMeeting() {
        return currentMeeting;
    }

    public void setCurrentMeeting(Meeting currentMeeting) {
        this.currentMeeting = currentMeeting;
    }

    public void addMeeting(Meeting meeting){
        meetingsQueue.add(meeting);
    }

    public Meeting peekMeeting(){
        return meetingsQueue.peek();
    }

    public Meeting checkMeetings(Date date){
        if (meetingsQueue.isEmpty() || 0 < meetingsQueue.peek().getStartDate().compareTo(date)){
            return null;
        }
        return meetingsQueue.peek();
    }

    public boolean isTimeSlotAvailable(Meeting meeting){
        return meetingsQueue.stream().noneMatch(meeting::isOverlapping);
    }

    public float estimateTemperatureForNeighbour(Meeting meeting){
        float estimatedTemperature = defaultTemperature;
        long timeBusy = 0;
        long meetingDuration = meeting.getEndDate().getTime() - meeting.getStartDate().getTime();
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
        if (null != getCurrentMeeting() && ID.equals(getCurrentMeeting().getMeetingID())){
            currentMeeting.setEndDate(new Date(Long.MIN_VALUE));
        }
        for (Meeting meeting:meetingsQueue){
            if (meeting.getMeetingID().equals(ID)){
                meetingsQueue.remove(meeting);
                return;
            }
        }
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
