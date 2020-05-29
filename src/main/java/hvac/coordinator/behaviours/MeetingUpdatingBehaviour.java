package hvac.coordinator.behaviours;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import hvac.calendar.CalendarException;
import hvac.database.Connection;
import hvac.time.DateTimeSimulator;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

public class MeetingUpdatingBehaviour extends TickerBehaviour {
    private Calendar calendar;
    public Connection database;

    public MeetingUpdatingBehaviour(Agent a, long period, Calendar calendar, Connection database) {
        super(a, period);
        this.calendar = calendar;
        this.database = database;
    }

    @Override
    protected void onTick() {
        List<Event> calendarEvents;
        try {
            calendarEvents = getCalendarEvents();
        } catch (CalendarException e) {
            System.err.println("Calendar threw an exception: ");
            e.printStackTrace();
            this.stop();
            return;
        }
        for(Event event : calendarEvents) {
            System.out.println(event.toString());
        }
    }

    public List<Event> getCalendarEvents() throws CalendarException
    {
        try
        {
            LocalDateTime now = DateTimeSimulator.getCurrentDate();
            DateTime minTime = new DateTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()),
                    TimeZone.getDefault());
            Events events = calendar.events().list("primary")
                    .setTimeMin(minTime)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            return events.getItems();
        } catch (Exception e) {
            throw new CalendarException(e);
        }
    }
}
