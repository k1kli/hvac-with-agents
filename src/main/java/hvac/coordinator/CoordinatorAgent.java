package hvac.coordinator;

import com.google.api.services.calendar.Calendar;
import hvac.calendar.CalendarException;
import hvac.calendar.CalendarWrapper;
import hvac.coordinator.behaviours.MeetingUpdatingBehaviour;
import hvac.database.Connection;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.roomcoordinator.RoomCoordinatorAgent;
import hvac.simulation.rooms.Room;
import hvac.simulation.rooms.RoomLink;
import hvac.simulation.rooms.RoomMap;
import hvac.simulation.rooms.RoomWall;
import hvac.util.df.DfHelpers;
import jade.content.lang.sl.SLCodec;
import jade.core.Runtime;
import jade.core.*;
import jade.domain.FIPANames;
import jade.wrapper.AgentContainer;

import java.util.ArrayList;

import static hvac.util.Helpers.initTimeFromArgs;
import static hvac.util.Helpers.loadMap;

public class CoordinatorAgent extends Agent {
    Connection database;
    Calendar calendar;
    CoordinatorContext context = new CoordinatorContext();
    @Override
    protected void setup() {
        getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(MeetingOntology.getInstance());
        context.getLogger().setAgentName("coordinator");
        if(!initTimeFromArgs(this, this::usage)) return;
        if(!DfHelpers.tryRegisterInDfWithServiceName(this, "coordinator")) return;
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
        deployAgents();
        this.addBehaviour(new MeetingUpdatingBehaviour(this, 1000, calendar, context));
    }

    private void deployAgents() {
        RoomMap roomMap = new RoomMap();
        loadMap(roomMap);

        for (Room room : roomMap.getRooms()) {
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.CONTAINER_NAME, Integer.toString(room.getId()));
            AgentContainer newContainer = Runtime.instance().createAgentContainer(profile);
            ArrayList<Integer> myNeighboursIds = new ArrayList<>();
            ArrayList<RoomWall> myWalls = new ArrayList<>();
            for (RoomLink roomLink : roomMap.getNeighbors(room)){
                myNeighboursIds.add(roomLink.getNeighbor().getId());
                myWalls.add(roomLink.getWall());
            }
            try {
                newContainer.createNewAgent("room-coordinator-" + room.getId(),
                        RoomCoordinatorAgent.class.getCanonicalName(),
                        new Object[]{
                                getArguments()[0],
                                getArguments()[1],
                                room.getId(),
                                getAID(),
                                myNeighboursIds,
                                myWalls,
                                room.isMeetingRoom()}).start();

                newContainer.createNewAgent("upkeeper-" + room.getId(),
                        "hvac.roomupkeeper.RoomUpkeeperAgent",
                        new Object[]{
                                getArguments()[0],
                                getArguments()[1],
                                room.getId(),
                                room.getArea()}).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(room.isMeetingRoom())
            context.addRoom(room.getSeats(),
                    new AID("room-coordinator-" + room.getId() + "@" + newContainer.getPlatformName(),AID.ISGUID));
        }

        context.getLogger().log("Deployed all room coordinators and upkeepers");
    }

    private void usage(String err) {
        System.err.println("-------- Coordinator agent usage --------------");
        System.err.println("simulation:hvac.coordinator.CoordinatorAgent(timeScale, start_date)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("err:" + err);
    }
}
