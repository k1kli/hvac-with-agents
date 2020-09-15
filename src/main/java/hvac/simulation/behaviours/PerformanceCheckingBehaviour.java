package hvac.simulation.behaviours;

import hvac.ontologies.meeting.Meeting;
import hvac.simulation.SimulationContext;
import hvac.simulation.rooms.RoomClimate;
import hvac.time.DateTimeSimulator;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.time.LocalDateTime;

public class PerformanceCheckingBehaviour extends TickerBehaviour {
    private final SimulationContext context;
    private static final int MINUTES_BETWEEN_UPDATES = 1;

    public PerformanceCheckingBehaviour(Agent agent, SimulationContext context) {
        super(agent, (long) (MINUTES_BETWEEN_UPDATES * 60 * 1000 / DateTimeSimulator.getTimeScale()));
        this.context = context;
    }

    @Override
    protected void onTick() {
        LocalDateTime now = DateTimeSimulator.getCurrentDate();
        for (Meeting meeting : context.getCalendarMeetings()) {
            if (meeting.getLocalStartDate().isBefore(now) && meeting.getLocalEndDate().isAfter(now)) {
                RoomClimate climate = context.getClimates().get(meeting.getRoomID());
                float unhappiness = Math.abs(meeting.getTemperature() - climate.getTemperature()) / meeting.getTemperature()
                        + Math.abs(0.5f - climate.getRelativeHumidity()) / 0.5f
                        + Math.max(1 - climate.getAirQuality(), 0);
                context.addEmployeesUnhappiness(unhappiness);
            }
        }
    }
}
