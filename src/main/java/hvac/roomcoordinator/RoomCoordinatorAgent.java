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

@SuppressWarnings("unused")
public class RoomCoordinatorAgent extends Agent {
    private RoomContext roomContext;
    private Integer[] Ids;
    private RoomWall[] RoomWalls;
    private int roomsProcessed = 0;

    @Override
    protected void setup() {
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(MeetingOntology.getInstance());
        roomContext = getContext();
        if(roomContext == null) {
            doDelete();
        }
    }

    private RoomContext getContext() {
        int myRoomId;
        try {
            myRoomId = Integer.parseInt(getArguments()[0].toString());
        } catch (NumberFormatException e) {
            usage("room Id is not valid");
            return null;
        }
        if(!DfHelpers.tryRegisterInDfWithServiceName(this, "room-coordinator-" + myRoomId)) {
            usage("room cannot be registered in DF");
            return null;
        }
        RoomContext newRoomContext = new RoomContext(myRoomId, (AID) getArguments()[1]);
        Ids = (Integer[]) getArguments()[2];
        RoomWalls = (RoomWall[]) getArguments()[3];

        addBehaviour(new FindingBehaviour(this, "upkeeper-" + myRoomId,
                upkeeperDescriptor->{
                    newRoomContext.setMyRoomUpkeeper(upkeeperDescriptor.getName());
                    processRoom();}));

        return newRoomContext;
    }

    private void processRoom(){
        if (Ids.length == roomsProcessed) {
            RoomCoordinatorTickerBehaviour tickerBehaviour = new RoomCoordinatorTickerBehaviour(this, 1000, roomContext);
            addBehaviour(tickerBehaviour);
            addBehaviour(new RoomCoordinatorCyclicBehaviour(this, roomContext, tickerBehaviour));
            return;
        }
        addBehaviour(new FindingBehaviour(this, "room-coordinator-" + Ids[roomsProcessed],
                neighbourDescriptor->{
                    roomContext.addMyNeighbour(neighbourDescriptor.getName(),RoomWalls[roomsProcessed]);
                    roomsProcessed++;
                    processRoom();}));
    }


    private void usage(String err) {
        System.err.println("-------- Room Coordinator agent usage --------------");
        System.err.println("simulation:hvac.roomcoordinator.RoomCoordinatorAgent(RoomId, CoordinatorAID, NeighboursIds, RoomWalls)");
        System.err.println("RoomId - integer uniquely identifying the room of this agent");
        System.err.println("CoordinatorAID - AID of Coordinator");
        System.err.println("NeighboursIds - array of neighbours' Ids");
        System.err.println("RoomWalls - array of walls' properties");
        System.err.println("err:" + err);
    }
}
