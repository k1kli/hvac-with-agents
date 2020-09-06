package hvac.roomupkeeper;

import hvac.ontologies.meeting.Meeting;
import hvac.time.DateTimeSimulator;
import hvac.util.Conversions;
import hvac.util.Logger;
import jade.core.AID;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class RoomUpkeeperContext {
    private AID weatherForecaster;
    private AID simulationAgent;
    private final int myRoomId;

    private Queue<Meeting> meetingQueue;
    private static final float relativeHumidityToMaintain = 0.5f;
    private final float myRoomArea;

    private final static float perPersonRequiredVentilation = 0.0025f;//2.5L/s = 0.0025m^3/s
    private final static float perM2RequiredVentilation = 0.0003f;//0.3L/s/m^2 = 0.0003m^3/s/m^2
    private final Logger logger = new Logger();

    public RoomUpkeeperContext(int myRoomId, float myRoomArea) {
        this.myRoomId = myRoomId;
        this.myRoomArea = myRoomArea;
        this.meetingQueue = new ArrayDeque<>();
    }


    /**
     * Next meeting that will be held in this room
     * room will be accomodated to its required conditions
     * is null if no new meeting is scheduled
     */
    public Meeting getNextMeeting() {
        return meetingQueue.peek();
    }

    /**
     * Removes all meetings for which end date is in the past
     */
    public void removeCompleteMeetings() {
        while(meetingQueue.peek() != null
                && Conversions.toLocalDateTime(meetingQueue.peek().getEndDate())
                .isBefore(DateTimeSimulator.getCurrentDate())) {
            meetingQueue.remove();
        }
    }

    public void setMeetingQueue(List<Meeting> meetings) {
        meetingQueue = meetings.stream()
                .sorted(Comparator.comparing(Meeting::getStartDate))
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    public AID getWeatherForecaster() {
        return weatherForecaster;
    }

    public AID getSimulationAgent() {
        return simulationAgent;
    }

    public int getMyRoomId() {
        return myRoomId;
    }

    public float getRelativeHumidityToMaintain() {
        return relativeHumidityToMaintain;
    }

    public float getRequiredVentilation() {
        return (getNextMeeting() != null ? perPersonRequiredVentilation*getNextMeeting().getPeopleInRoom() : 0.0f)
                + perM2RequiredVentilation * myRoomArea;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setWeatherForecaster(AID weatherForecaster) {
        this.weatherForecaster = weatherForecaster;
    }

    public void setSimulationAgent(AID simulationAgent) {
        this.simulationAgent = simulationAgent;
    }
}
