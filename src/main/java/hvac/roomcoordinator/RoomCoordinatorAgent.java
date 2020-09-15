package hvac.roomcoordinator;

import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.presence.PresenceOntology;
import hvac.roomcoordinator.behaviours.ConditionsInformingBehaviour;
import hvac.roomcoordinator.behaviours.MeetingHandlingBehaviour;
import hvac.roomcoordinator.behaviours.PeopleHandlingBehaviour;
import hvac.roomcoordinator.behaviours.UpkeeperManagingBehaviour;
import hvac.simulation.rooms.RoomWall;
import hvac.util.behaviours.NotUnderstoodBehaviour;
import hvac.util.df.DfHelpers;
import hvac.util.df.FindingBehaviour;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;

import java.util.ArrayList;

import static hvac.util.Helpers.initTimeFromArgs;

public class RoomCoordinatorAgent extends Agent {
    private RoomContext roomContext;
    private ArrayList<Integer> Ids;
    private ArrayList<RoomWall> RoomWalls;
    private int roomsProcessed = 0;

    @Override
    protected void setup() {
        if(!initTimeFromArgs(this, this::usage)) return;
        getContentManager().registerLanguage(new SLCodec(),
                FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(MeetingOntology.getInstance());
        getContentManager().registerOntology(PresenceOntology.getInstance());
        roomContext = getContext();
        if(roomContext == null) {
            doDelete();
        }
    }

    @SuppressWarnings("unchecked")
    private RoomContext getContext() {
        if(getArguments().length != 8) {
            usage("incorrect number of arguments, required:8, provided: " + getArguments().length);
        }
        int myRoomId;
        try {
            myRoomId = Integer.parseInt(getArguments()[2].toString());
        } catch (NumberFormatException e) {
            usage("room Id is not valid");
            return null;
        }
        if(!DfHelpers.tryRegisterInDfWithServiceName(this, "room-coordinator-" + myRoomId)) {
            usage("room cannot be registered in DF");
            return null;
        }
        int seats;
        try {
            seats = Integer.parseInt(getArguments()[7].toString());
        } catch (NumberFormatException e) {
            usage("seats amount is not valid");
            return null;
        }
        RoomContext newRoomContext = new RoomContext(myRoomId,
                (AID) getArguments()[3],
                (boolean) getArguments()[6],
                seats);
        newRoomContext.getLogger().setAgentName("room coordinator (" + newRoomContext.getMyRoomId() + ")");
        try {
            Ids = (ArrayList<Integer>) getArguments()[4];
        } catch (Exception e) {
            usage("room ids list is not valid");
            return null;
        }
        try {
            RoomWalls = (ArrayList<RoomWall>) getArguments()[5];
        } catch (Exception e) {
            usage("room neighbour list is not valid");
            return null;
        }
        addBehaviour(new FindingBehaviour(this, "upkeeper-" + myRoomId,
                upkeeperDescriptor->{
                    newRoomContext.setMyRoomUpkeeper(upkeeperDescriptor.getName());
                    processRoom();}));
        //uncomment this to test coordinator-upkeeper communication
        //if(myRoomId == 1) newRoomContext.addMeeting(new Meeting("abc", Conversions.toDate(DateTimeSimulator.getCurrentDate().plusMinutes(40)),
        //        Conversions.toDate(DateTimeSimulator.getCurrentDate().plusHours(3)),3, 300.0f));
        return newRoomContext;
    }

    private void processRoom(){
        if (Ids.size() == roomsProcessed) {
            UpkeeperManagingBehaviour upkeeperManagingBehaviour = new UpkeeperManagingBehaviour(this, 1000, roomContext);
            addBehaviour(upkeeperManagingBehaviour);
            ConditionsInformingBehaviour conditionsInformingBehaviour = new ConditionsInformingBehaviour(this, roomContext);
            addBehaviour(conditionsInformingBehaviour);
            if(roomContext.isMeetingRoom()) {
                MeetingHandlingBehaviour meetingHandlingBehaviour = new MeetingHandlingBehaviour(this, roomContext, upkeeperManagingBehaviour);
                addBehaviour(meetingHandlingBehaviour);

                addBehaviour(new NotUnderstoodBehaviour(this, roomContext.getLogger(),
                        upkeeperManagingBehaviour.getTemplate(),
                        conditionsInformingBehaviour.getTemplate(),
                        meetingHandlingBehaviour.getTemplate()));
            } else {
                PeopleHandlingBehaviour peopleHandlingBehaviour = new PeopleHandlingBehaviour(this, roomContext, upkeeperManagingBehaviour);
                addBehaviour(peopleHandlingBehaviour);

                addBehaviour(new NotUnderstoodBehaviour(this, roomContext.getLogger(),
                        upkeeperManagingBehaviour.getTemplate(),
                        conditionsInformingBehaviour.getTemplate(),
                        peopleHandlingBehaviour.getTemplate()));
            }
            roomContext.getLogger().log("Successfully initialized and found all my neighbours and upkeeper");
            return;
        }
        addBehaviour(new FindingBehaviour(this, "room-coordinator-" + Ids.get(roomsProcessed),
                neighbourDescriptor->{
                    roomContext.addMyNeighbour(neighbourDescriptor.getName(),RoomWalls.get(roomsProcessed));
                    roomsProcessed++;
                    processRoom();}));
    }


    private void usage(String err) {
        System.err.println("-------- Room Coordinator agent usage --------------");
        System.err.println("simulation:hvac.roomcoordinator.RoomCoordinatorAgent(timeScale, start_date, RoomId, CoordinatorAID, NeighboursIds, RoomWalls, isMeetingRoom, seats)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("RoomId - integer uniquely identifying the room of this agent");
        System.err.println("CoordinatorAID - AID of Coordinator");
        System.err.println("NeighboursIds - array of neighbours' Ids");
        System.err.println("RoomWalls - array of walls' properties");
        System.err.println("IsMeetingRoom - boolean indicating if this room will be used for meetings");
        System.err.println("seats - number of seats in this room");
        System.err.println("err:" + err);
    }
}
