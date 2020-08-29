package hvac.roomupkeeper;

import hvac.ontologies.meeting.Meeting;
import hvac.util.Logger;
import jade.core.AID;

public class RoomUpkeeperContext {
    private final AID weatherForecaster;
    private final AID simulationAgent;
    private final int myRoomId;

    /**
     * Next meeting that will be held in this room
     * room will be accomodated to its required conditions
     * is null if no new meeting is scheduled
     */
    private Meeting nextMeeting;
    private static final float relativeHumidityToMaintain = 0.5f;
    private final float myRoomArea;

    private final static float perPersonRequiredVentilation = 0.0025f;//2.5L/s = 0.0025m^3/s
    private final static float perM2RequiredVentilation = 0.0003f;//0.3L/s/m^2 = 0.0003m^3/s/m^2
    private final Logger logger = new Logger();

    public RoomUpkeeperContext(AID weatherForecaster, AID simulationAgent, int myRoomId, float myRoomArea) {
        this.weatherForecaster = weatherForecaster;
        this.simulationAgent = simulationAgent;
        this.myRoomId = myRoomId;
        this.myRoomArea = myRoomArea;
        this.nextMeeting = null;
    }

    public Meeting getNextMeeting() {
        return nextMeeting;
    }

    public void setNextMeeting(Meeting nextMeeting) {
        this.nextMeeting = nextMeeting;
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
        return (nextMeeting != null ? perPersonRequiredVentilation*nextMeeting.getPeopleInRoom() : 0.0f)
                + perM2RequiredVentilation * myRoomArea;
    }

    public Logger getLogger() {
        return logger;
    }
}
