package hvac.coordinator.behaviours;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import hvac.calendar.CalendarException;
import hvac.coordinator.CoordinatorContext;
import hvac.coordinator.Meeting;
import hvac.database.entities.Employee;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.meeting.Request;
import hvac.ontologies.meeting.RequestStatus;
import hvac.time.DateTimeSimulator;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

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
    private final Calendar calendar;
    private final CoordinatorContext context;

    public MeetingUpdatingBehaviour(Agent a, long period, Calendar calendar,
                                    CoordinatorContext context) {
        super(a, period);
        this.calendar = calendar;
        this.context = context;
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
        List<Meeting> meetingsToAdd = filterMeetings(calendarMeetings);
        context.getMeetingsToAssign()
                .putAll(meetingsToAdd.stream()
                        .collect(Collectors.toMap(Meeting::getId, Function.identity())));
        for(Meeting meeting : meetingsToAdd) {
            System.out.println(meeting.getId() + " " + meeting.getStartDate() + " " + meeting.getEndDate());
            for(Employee emp : meeting.getEmployees())
                System.out.println("    -"+emp.getAlias());
        }
        sendCFPs(meetingsToAdd);
    }

    public List<Event> getCalendarEvents() throws CalendarException
    {
        try
        {
            LocalDateTime now = DateTimeSimulator.getCurrentDate();

            DateTime minTime = new DateTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()),
                    TimeZone.getDefault());
            DateTime maxTime = new DateTime(Date.from(now.plusWeeks(1).atZone(ZoneId.systemDefault()).toInstant()),
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
                0,
                employeeSet);
    }

    public List<Meeting> filterMeetings(List<Meeting> fromCalendar)
    {
        return fromCalendar.stream()
                //only get meetings one week ahead
                .filter(m->m.getStartDate().isBefore(DateTimeSimulator.getCurrentDate().plusWeeks(1)))
                .filter(m->
                        !context.getAssignedMeetings().containsKey(m.getId()) &&
                        !context.getMeetingsToAssign().containsKey(m.getId()))
                .collect(Collectors.toList());
    }

    public void sendCFPs(List<Meeting> meetingsToAdd){
        for(Meeting meeting: meetingsToAdd){
            Set<AID> possibleRooms = context.getRoomsByNSeats(meeting.getEmployees().size());
            if (null == possibleRooms){
                context.getLogger().log("No rooms with " + meeting.getEmployees().size() + " or greater number of seats");
                return;
            }
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            msg.setConversationId(meeting.getId());
            for (AID receiver  : possibleRooms){
                msg.addReceiver(receiver);
            }
            Request request = new Request(new hvac.ontologies.meeting.Meeting(meeting), RequestStatus.OFFER);
            request.setStatus(RequestStatus.OFFER);
            msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
            msg.setOntology(MeetingOntology.getInstance().getName());
            try {
                myAgent.getContentManager().fillContent(msg, new Action(myAgent.getAID(), request));
            } catch (Exception e){e.printStackTrace();}
            context.getLogger().log("Sent CFP for meeting " + meeting.getId());
            myAgent.send(msg);
        }
    }
}
