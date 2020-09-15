package hvac.simulation.behaviours;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import hvac.calendar.CalendarException;
import hvac.database.entities.Employee;
import hvac.ontologies.meeting.Meeting;
import hvac.roomupkeeper.RoomUpkeeperAgentMessenger;
import hvac.simulation.SimulationContext;
import hvac.simulation.rooms.Room;
import hvac.time.DateTimeSimulator;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AgentlessModeHandlingBehaviour extends TickerBehaviour {
    private final SimulationContext context;
    private static final int MINUTES_BETWEEN_UPDATES = 5;
    private final Map<Integer, Meeting> currentMeetings = new HashMap<>();

    public AgentlessModeHandlingBehaviour(Agent agent, SimulationContext context) {
        super(agent, (long) (MINUTES_BETWEEN_UPDATES * 60 * 1000 / DateTimeSimulator.getTimeScale()));
        this.context = context;
        createIgnoreBehaviour(context);
    }

    private void createIgnoreBehaviour(SimulationContext context) {
        MessageTemplate messageTemplate = MessageTemplate.not(MessageTemplate.MatchAll());
        for (AID roomUpkeeper :
                context.getUpkeepers().values()) {
            messageTemplate = MessageTemplate.or(messageTemplate, MessageTemplate.MatchSender(roomUpkeeper));
        }
        MessageTemplate finalMessageTemplate = MessageTemplate.and(
                MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                        MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
                                MessageTemplate.MatchPerformative(ACLMessage.NOT_UNDERSTOOD))),
                messageTemplate);
        myAgent.addBehaviour(new CyclicBehaviour(myAgent) {
            @Override
            public void action() {
                if (null == myAgent.receive(finalMessageTemplate))
                    block();
            }
        });
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
        List<Meeting> calendarMeetings = constructFromCalendar(calendarEvents)
                .stream()
                .filter(meeting -> meeting.getLocalStartDate().isBefore(DateTimeSimulator.getCurrentDate().plusMinutes(MINUTES_BETWEEN_UPDATES)))
                .filter(meeting -> context.getCalendarMeetings()
                        .stream()
                        .noneMatch(meeting1 -> meeting1.getMeetingID().equals(meeting.getMeetingID())))
                .collect(Collectors.toList());
        updateCurrentMeetings();
        for (Meeting meeting : calendarMeetings) {
            List<Room> validRooms = getRoomsByNSeats(meeting.getPeopleInRoom());
            Optional<Room> firstValidRoom = validRooms.stream().findFirst();
            if (firstValidRoom.isPresent()) {
                context.getLogger().log("setting up meeting with id " + meeting.getMeetingID() + " " +
                        "in room with id " + firstValidRoom.get().getId());
                try {
                    float targetTemperature = 22 + 273; //TODO: Calculate this from DB of employees
                    meeting.setTemperature(targetTemperature);
                    ACLMessage msg = RoomUpkeeperAgentMessenger.prepareMantainConditions(
                            Collections.singletonList(meeting),
                            myAgent,
                            context.getUpkeepers().get(firstValidRoom.get().getId()));
                    myAgent.send(msg);
                } catch (Codec.CodecException | OntologyException e) {
                    e.printStackTrace();
                    continue;
                }
                meeting.setRoomID(firstValidRoom.get().getId());
                currentMeetings.put(firstValidRoom.get().getId(), meeting);
                context.getCalendarMeetings().add(meeting);
            } else {
                context.getLogger().log("No rooms with " + meeting.getPeopleInRoom() + " or more seats");
            }
        }
    }

    public List<Room> getRoomsByNSeats(int seats) {
        Map<Integer, List<Room>> roomsWithNSeats = context.getRoomMap().getRooms().stream()
                .filter(room -> !currentMeetings.containsKey(room.getId())
                        && room.getSeats() > seats).collect(Collectors.groupingBy(Room::getSeats));
        Optional<Integer> lowestSeatCount = roomsWithNSeats.keySet().stream().min(Integer::compareTo);
        return lowestSeatCount.map(roomsWithNSeats::get).orElse(Collections.emptyList());
    }

    private void updateCurrentMeetings() {
        currentMeetings.keySet()
                .removeIf(roomId -> currentMeetings.get(roomId).getLocalEndDate().isBefore(DateTimeSimulator.getCurrentDate()));
    }

    public List<Event> getCalendarEvents() throws CalendarException {
        try {
            LocalDateTime now = DateTimeSimulator.getCurrentDate();

            DateTime minTime = new DateTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()),
                    TimeZone.getDefault());
            DateTime maxTime = new DateTime(Date.from(now.plusDays(1).atZone(ZoneId.systemDefault()).toInstant()),
                    TimeZone.getDefault());
            //System.out.println("Available calendars: " + calendar.calendarList().list().execute()); // to check the ID of the source calendar
            Events events = context.getCalendar().events().list("ruheb94q1rga1gfonpr949iomc@group.calendar.google.com")
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

    public List<Meeting> constructFromCalendar(List<Event> events) {
        return events.stream().map(this::constructFromCalendar).collect(Collectors.toList());
    }

    public Meeting constructFromCalendar(Event event) {
        Function<EventDateTime, LocalDateTime> convertToLocal =
                dt -> Instant
                        .ofEpochMilli(dt.getDateTime().getValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
        Set<Employee> employeeSet = Arrays.stream(event.getDescription()
                .split("\n"))
                .map(Employee::new)
                .collect(Collectors.toSet());
        return new Meeting(new hvac.coordinator.Meeting(
                event.getId(),
                convertToLocal.apply(event.getStart()),
                convertToLocal.apply(event.getEnd()),
                null,
                employeeSet));
    }
}
