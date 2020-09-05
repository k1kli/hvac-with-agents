package hvac.roomcoordinator;

import hvac.ontologies.meeting.MeetingOntology;
import hvac.roomcoordinator.behaviours.RoomCoordinatorCyclicBehaviour;
import hvac.roomcoordinator.behaviours.RoomCoordinatorTickerBehaviour;
import hvac.simulation.rooms.RoomWall;
import hvac.util.df.DfHelpers;
import hvac.util.df.FindingBehaviour;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;

import java.util.ArrayList;

import static hvac.util.Helpers.initTimeFromArgs;

@SuppressWarnings("unused")
public class RoomCoordinatorAgent extends Agent {
    private RoomContext roomContext;
    private ArrayList<Integer> Ids;
    private ArrayList<RoomWall> RoomWalls;
    private int roomsProcessed = 0;

    @Override
    protected void setup() {
        if(!initTimeFromArgs(this, this::usage)) return;
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(MeetingOntology.getInstance());
        roomContext = getContext();
        if(roomContext == null) {
            doDelete();
        }
    }

    @SuppressWarnings("unchecked")
    private RoomContext getContext() {
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
        RoomContext newRoomContext = new RoomContext(myRoomId, (AID) getArguments()[3]);
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

        return newRoomContext;
    }

    private void processRoom(){
        if (Ids.size() == roomsProcessed) {
            RoomCoordinatorTickerBehaviour tickerBehaviour = new RoomCoordinatorTickerBehaviour(this, 1000, roomContext);
            addBehaviour(tickerBehaviour);
            addBehaviour(new RoomCoordinatorCyclicBehaviour(this, roomContext, tickerBehaviour));
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
        System.err.println("simulation:hvac.roomcoordinator.RoomCoordinatorAgent(RoomId, CoordinatorAID, NeighboursIds, RoomWalls)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("RoomId - integer uniquely identifying the room of this agent");
        System.err.println("CoordinatorAID - AID of Coordinator");
        System.err.println("NeighboursIds - array of neighbours' Ids");
        System.err.println("RoomWalls - array of walls' properties");
        System.err.println("err:" + err);
    }
}
