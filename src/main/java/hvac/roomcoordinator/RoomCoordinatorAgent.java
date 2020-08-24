package hvac.roomcoordinator;

import hvac.ontologies.meeting.Meeting;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.meeting.Request;
import hvac.ontologies.meeting.RequestStatus;
import hvac.simulation.rooms.RoomWall;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Date;
import java.util.HashMap;

@SuppressWarnings("unused")
public class RoomCoordinatorAgent extends Agent {
    private RoomContext roomContext;
    private TickerBehaviour tickerBehaviour;
    private static Codec codec = new SLCodec();
    private static Ontology meetingOntology = MeetingOntology.getInstance();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(meetingOntology);
        Object[] args =  getArguments();
        HashMap<AID, RoomWall> roomConfig = null;
        //checking if map was passed correctly, unfortunately this is IMO the best and proper way
        // to detect and avoid errors and warnings
        try {
            @SuppressWarnings("unchecked")
            HashMap<AID, RoomWall> hashMap = (HashMap<AID, RoomWall>) args[2];
            hashMap.keySet().forEach(x -> {});
            hashMap.values().forEach(x -> {});
            roomConfig = hashMap;
        }
        catch (ClassCastException e) {e.printStackTrace();doDelete();}
        roomContext = new RoomContext(
                (AID) args[0],
                (AID) args[1],
                roomConfig
        );
        tickerBehaviour = new TickerBehaviour(this, 1000) {

            @Override
            protected void onTick() {
                if (null == roomContext.getCurrentMeeting()){
                    Meeting meeting = roomContext.checkMeetings(new Date());
                    if (null != meeting){
                        roomContext.setCurrentMeeting(meeting);
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        Request request = new Request(roomContext.getCurrentMeeting(), RequestStatus.EXECUTE);
                        sendUpdateToUpkeeper(msg, request);
                        this.reset(meeting.getEndDate().getTime() - new Date().getTime());
                    }
                    else{
                        if (null != roomContext.peekMeeting()){
                            this.reset(roomContext.peekMeeting().getStartDate().getTime() - new Date().getTime());
                        }
                        else{
                            this.reset(Long.MAX_VALUE);
                        }
                    }
                }
                else{
                    if (roomContext.getCurrentMeeting().getEndDate().before(new Date())){
                        Meeting currentMeeting = roomContext.getCurrentMeeting();
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        Request request = new Request(currentMeeting, RequestStatus.FINISHED);
                        sendUpdateToUpkeeper(msg, request);
                        roomContext.setCurrentMeeting(null);
                        roomContext.removeMeeting(currentMeeting.getMeetingID());
                        this.reset(0);
                    }
                    else{
                        this.reset(1000);
                    }
                }
            }
        };
        addBehaviour(tickerBehaviour);
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    handleMessage(msg);
                }
                else {
                    block();
                }
            }
        });
    }

    private void handleMessage(ACLMessage msg){
        switch(msg.getPerformative()) {
            case ACLMessage.CFP:
                handleCFP(msg);
                break;
            case ACLMessage.REQUEST:
                handleRequest(msg);
                break;
            case ACLMessage.INFORM:
                handleInform(msg);
                break;
            case ACLMessage.ACCEPT_PROPOSAL:
                handleAcceptProposal(msg);
                break;
            case ACLMessage.CANCEL:
                handleCancel(msg);
                break;
            case ACLMessage.REJECT_PROPOSAL:
            case ACLMessage.NOT_UNDERSTOOD:
                break;
            default:
                notUnderstood(msg);
        }
    }

    private void handleCFP(ACLMessage msg){
        Request request = extractRequest(msg);
        if (null == request){
            return;
        }
        if (! roomContext.isTimeSlotAvailable(request.getMeeting())){
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            request.setStatus(RequestStatus.FAILED);
            fillAndSend(reply, request);
            return;
        }
        ACLMessage requestForecast = msg.createReply();
        requestForecast.clearAllReceiver();
        for (AID neighbour:roomContext.getMyNeighbours().keySet()){
            requestForecast.addReceiver(neighbour);
        }
        request.setStatus(RequestStatus.FORECAST);
        fillAndSend(requestForecast,request);
        roomContext.newForecastEntry(request.getMeeting());
    }

    private void handleRequest(ACLMessage msg){
        Request request = extractRequest(msg);
        if (null == request){
            return;
        }
        request.getMeeting().setTemperature(roomContext.estimateTemperatureForNeighbour(request.getMeeting()));
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        request.setStatus(RequestStatus.ESTIMATION);
        fillAndSend(reply, request);
    }

    private void handleInform(ACLMessage msg){
        Request request = extractRequest(msg);
        if (null == request){
            return;
        }
        roomContext.addNeighbourForecast(
                request.getMeeting().getMeetingID(),
                msg.getSender(),
                request.getMeeting().getTemperature());
        if (! roomContext.isForecastCompleted(request.getMeeting().getMeetingID())){
            return;
        }
        request.getMeeting().setTemperature(roomContext.computeForecast(request.getMeeting().getMeetingID()));
        roomContext.removeForecastEntry(request.getMeeting().getMeetingID());
        ACLMessage reply = msg.createReply();
        msg.setPerformative(ACLMessage.PROPOSE);
        msg.clearAllReceiver();
        msg.addReceiver(roomContext.getCoordinator());
        request.setStatus(RequestStatus.OFFER);
        fillAndSend(reply, request);
    }

    private void handleAcceptProposal(ACLMessage msg){
        Request request = extractRequest(msg);
        if (null == request) {
            return;
        }
        ACLMessage reply = msg.createReply();
        if (roomContext.isTimeSlotAvailable(request.getMeeting())){
            reply.setPerformative(ACLMessage.INFORM);
            request.setStatus(RequestStatus.RESERVED);
            roomContext.addMeeting(request.getMeeting());
        }
        else {
            reply.setPerformative(ACLMessage.REFUSE);
            request.setStatus(RequestStatus.FAILED);
        }
        fillAndSend(reply, request);
        tickerBehaviour.reset(0);
    }

    private void handleCancel(ACLMessage msg){
        Request request = extractRequest(msg);
        if (null == request){
            return;
        }
        roomContext.removeMeeting(request.getMeeting().getMeetingID());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.CONFIRM);
        request.setStatus(RequestStatus.CANCELLED);
        fillAndSend(reply, request);
        tickerBehaviour.reset(0);
    }

    private void notUnderstood(ACLMessage msg){
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        reply.setContent(msg.getContent());
        send(reply);
    }

    private Request extractRequest(ACLMessage msg){
        Request request = null;
        try {
            request = (Request) getContentManager().extractContent(msg);
        } catch (Exception e){e.printStackTrace();}
        if (null == request || null == request.getMeeting()){
            notUnderstood(msg);
            return null;
        }
        return request;
    }

    private void fillAndSend(ACLMessage msg, Request request){
        try {
            getContentManager().fillContent(msg, request);
        } catch (Exception e){e.printStackTrace();}
        send(msg);
    }

    private void sendUpdateToUpkeeper(ACLMessage msg, Request request){
        msg.setConversationId(roomContext.getCurrentMeeting().getMeetingID());
        msg.setLanguage(codec.getName());
        msg.setOntology(meetingOntology.getName());
        msg.addReceiver(roomContext.getMyRoomUpkeeper());
        fillAndSend(msg, request);
    }
}
