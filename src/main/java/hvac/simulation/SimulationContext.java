package hvac.simulation;

import com.google.api.services.calendar.Calendar;
import hvac.database.Connection;
import hvac.ontologies.meeting.Meeting;
import hvac.simulation.rooms.RoomClimate;
import hvac.simulation.rooms.RoomMap;
import hvac.util.Logger;
import jade.core.AID;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class SimulationContext {
    private final RoomMap roomMap = new RoomMap();
    private final Hashtable<Integer, RoomClimate> climates = new Hashtable<>();
    private final OutsideClimate outsideClimate = new OutsideClimate();
    private final Logger logger = new Logger();
    private final Connection connection;
    private final boolean isAgentless;
    private final Hashtable<Integer, AID> upkeepers = new Hashtable<>();
    private Calendar calendar;
    private final List<Meeting> calendarMeetings = new ArrayList<>();
    private float totalEnergyUsed = 0; //in kJ
    private float employeesUnhappiness = 0;

    public SimulationContext(boolean isAgentless, Connection connection) {
        this.isAgentless = isAgentless;
        this.connection = connection;
    }

    public RoomMap getRoomMap() {
        return roomMap;
    }

    public Hashtable<Integer, RoomClimate> getClimates() {
        return climates;
    }

    public OutsideClimate getOutsideClimate() {
        return outsideClimate;
    }

    public Logger getLogger() {
        return logger;
    }

    @SuppressWarnings("unused")
    public boolean isAgentless() {
        return isAgentless;
    }

    public Connection getConnection() {
        return connection;
    }

    public Hashtable<Integer, AID> getUpkeepers() {
        return upkeepers;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public List<Meeting> getCalendarMeetings() {
        return calendarMeetings;
    }

    public float getTotalEnergyUsed() {
        return totalEnergyUsed;
    }

    public void addEnergyUsage(float energyUsed) {
        totalEnergyUsed += energyUsed;
    }

    public float getEmployeesUnhappiness() {
        return employeesUnhappiness;
    }

    public void addEmployeesUnhappiness(float unhappiness){
        employeesUnhappiness += unhappiness;
    }
}
