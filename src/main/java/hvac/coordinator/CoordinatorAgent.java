package hvac.coordinator;

import com.google.api.services.calendar.Calendar;
import hvac.calendar.CalendarException;
import hvac.calendar.CalendarWrapper;
import hvac.coordinator.behaviours.MeetingUpdatingBehaviour;
import hvac.database.Connection;
import hvac.time.DateTimeSimulator;
import jade.core.Agent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@SuppressWarnings("unused")
public class CoordinatorAgent extends Agent {
    Connection database;
    Calendar calendar;
    CoordinatorContext context = new CoordinatorContext();
    @Override
    protected void setup() {
        initTimeFromArgs();
        database = new Connection();
        CalendarWrapper wrapper = new CalendarWrapper();
        try {
            calendar = wrapper.getCalendarService();
        } catch (CalendarException e) {
            System.err.println("failed to obtain calendar");
            e.printStackTrace();
            doDelete();
            return;
        }
        this.addBehaviour(new MeetingUpdatingBehaviour(this, 1000, calendar, context));
    }

    private void initTimeFromArgs() {
        if (getArguments() == null || getArguments().length != 2) {
            usage("Wrong args num");
            doDelete();
            return;
        }
        LocalDateTime startTime;
        float timeScale;
        try {
            timeScale = Float.parseFloat(getArguments()[0].toString());
            startTime = LocalDateTime.parse(getArguments()[1].toString(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (NumberFormatException | DateTimeParseException e) {
            usage(e.getMessage());
            doDelete();
            return;
        }
        DateTimeSimulator.init(startTime, timeScale);
    }

    private void usage(String err) {
        System.err.println("-------- Coordinator agent usage --------------");
        System.err.println("simulation:hvac.coordinator.CoordinatorAgent(timeScale, start_date)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("err:" + err);
    }
}