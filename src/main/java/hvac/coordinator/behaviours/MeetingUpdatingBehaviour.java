package hvac.coordinator.behaviours;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import hvac.calendar.CalendarException;
import hvac.database.Connection;
import hvac.database.entities.Employee;
import hvac.database.entities.Meeting;
import hvac.time.DateTimeSimulator;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        List<Meeting> calendarMeetings = constructFromCalendar(calendarEvents);
        List<Meeting> matchingFromDb = getMatchingFromDb(calendarMeetings);
        Set<String> idsInDb = matchingFromDb.stream().map(Meeting::getId)
                .collect(Collectors.toSet());
        List<Meeting> notInDb = calendarMeetings.stream()
                .filter(meeting -> !idsInDb.contains(meeting.getId()))
                .collect(Collectors.toList());
        addMeetingsToDb(notInDb);
        for(Meeting meeting : matchingFromDb) {
            System.out.println(meeting.getId() + " " + meeting.getStartDate() + " " + meeting.getEndDate());
            for(Employee emp : meeting.getEmployees())
                System.out.println("    -"+emp.getAlias());
        }
    }

    public List<Event> getCalendarEvents() throws CalendarException
    {
        try
        {
            LocalDateTime now = DateTimeSimulator.getCurrentDate();

            DateTime minTime = new DateTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()),
                    TimeZone.getDefault());
            DateTime maxTime = new DateTime(Date.from(now.plusMonths(1).atZone(ZoneId.systemDefault()).toInstant()),
                    TimeZone.getDefault());
            Events events = calendar.events().list("primary")
                    .setTimeMin(minTime)
                    .setTimeMax(maxTime)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            return events.getItems();
        } catch (Exception e) {
            throw new CalendarException(e);
        }
    }

    public List<Meeting> constructFromCalendar(List<Event> events)
    {
        return events.stream().map(this::constructFromCalendar).collect(Collectors.toList());
    }

    public Meeting constructFromCalendar(Event event)
    {
        Function<EventDateTime, LocalDateTime> convertToLocal =
                dt-> Instant
                        .ofEpochMilli(dt.getDateTime().getValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
        Set<Employee> employeeSet = Arrays.stream(event.getDescription()
                .split("\n"))
                .map(Employee::new)
                .collect(Collectors.toSet());
        return new Meeting(
                event.getId(),
                convertToLocal.apply(event.getStart()),
                convertToLocal.apply(event.getEnd()),
                event.getId(),
                employeeSet);
    }

    public List<Meeting> getMatchingFromDb(List<Meeting> fromCalendar)
    {
        Session session = database.createEntityManager().unwrap(Session.class);
        List<Meeting> result = session.createQuery("from Meeting m join fetch m.employees where m.id in :ids", Meeting.class)
                        .setParameter("ids", fromCalendar
                            .stream()
                            .map(Meeting::getId)
                            .collect(Collectors.toList()))
                        .getResultList();
        session.close();
        return result;
    }
    public void addMeetingsToDb(List<Meeting> meetings)
    {
        Session session = database.createEntityManager().unwrap(Session.class);
        Transaction t = session.beginTransaction();
        for(Meeting m : meetings)
        {
            session.save(m);
        }
        t.commit();
        session.close();
    }
}
