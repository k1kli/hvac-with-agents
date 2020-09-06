package hvac.coordinator.behaviours;

import hvac.coordinator.CoordinatorContext;
import hvac.coordinator.Meeting;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.meeting.Request;
import hvac.ontologies.meeting.RequestStatus;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MeetingAssignerBehaviour extends SimpleBehaviour {
    private final Meeting meeting;
    private final CoordinatorContext context;
    private final MessageTemplate template;
    private final Map<AID, Request> replies = new HashMap<>();
    private int roomsNegotiating;
    private AID candidate = null;
    private boolean done = false;
    private boolean sendCFPs = true;
    private final float targetTemperature = 22; //TODO: Calculate this from DB of employees

    public MeetingAssignerBehaviour(Agent a, Meeting meeting, CoordinatorContext context) {
        super(a);
        this.meeting = meeting;
        this.context = context;
        template = MessageTemplate.and(
                MessageTemplate.MatchConversationId(meeting.getId()),
                MessageTemplate.and(
                        MessageTemplate.MatchOntology(MeetingOntology.getInstance().getName()),
                        MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0)
                ));
    }

    @Override
    public void action() {
        if (sendCFPs) {
            sendCFPs(meeting.getEmployees().size());
            sendCFPs = false;
        }
        else {
            ACLMessage msg = myAgent.receive(template);
            if (msg != null) {
                handleMessage(msg);
            } else {
                block();
            }
        }
    }

    @Override
    public boolean done() {
        return done;
    }

    public void sendCFPs(int minSeats){
        Set<AID> possibleRooms = context.getRoomsByNSeats(minSeats);
        if (null == possibleRooms){
            context.getLogger().log("No rooms with " + minSeats + " or more seats");
            done = true;
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
        roomsNegotiating = possibleRooms.size();
        myAgent.send(msg);
    }

    private void handleMessage(ACLMessage msg){
        switch(msg.getPerformative()) {
            case ACLMessage.PROPOSE:
                handlePropose(msg);
                break;
            case ACLMessage.REFUSE:
                handleRefuse(msg);
                break;
            case ACLMessage.FAILURE:
                handleFailure(msg);
                break;
            case ACLMessage.INFORM:
                handleInform(msg);
                break;
            case ACLMessage.NOT_UNDERSTOOD:
                break;
            default:
                notUnderstood(msg);
        }
    }

    private void handlePropose(ACLMessage msg){
        extractRequest(msg);
        if(replies.size() == roomsNegotiating){
            selectBestCandidate();
        }
    }

    @SuppressWarnings("unused")
    private void handleRefuse(ACLMessage msg){
        roomsNegotiating--;
        if(replies.size() == roomsNegotiating){
            selectBestCandidate();
        }
    }

    @SuppressWarnings("unused")
    private void handleFailure(ACLMessage msg){
        selectBestCandidate();
    }

    private void handleInform(ACLMessage msg) {
        if (! msg.getSender().equals(candidate)){
            context.getLogger().log("Candidate: " + candidate);
            notUnderstood(msg);
            return;
        }
        meeting.setRoomCoordinator(candidate);
        context.getAssignedMeetings().put(meeting.getId(),meeting);
        context.getMeetingsToAssign().remove(meeting.getId());
        context.getLogger().log("Meeting " + meeting.getId() + " successfully reserved by " + candidate.getName());
        //TODO: Inform Employees that meeting will be held in this room
        done = true;
    }

    private void notUnderstood(ACLMessage msg){
        context.getLogger().log("Not understood " + msg);
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        reply.setContent(msg.getContent());
        myAgent.send(reply);
    }

    private void extractRequest(ACLMessage msg){
        Request request = null;
        try {
            request = (Request) ((Action) myAgent.getContentManager().extractContent(msg)).getAction();
        } catch (Exception e){e.printStackTrace();}
        if (null == request || null == request.getMeeting()){
            notUnderstood(msg);
            return;
        }
        replies.put(msg.getSender(), request);
    }

    private void selectBestCandidate(){
        int currentRoomSize = meeting.getEmployees().size();
        if (null != candidate){
            currentRoomSize = replies.get(candidate).getMeeting().getPeopleInRoom();
            replies.remove(candidate);
            roomsNegotiating--;
        }
        if (replies.isEmpty()){
            sendCFPs(currentRoomSize + 1);
            return;
        }
        float minDiff = Float.MAX_VALUE;
        for (AID freeRooms: replies.keySet()){
            float forecastedDiff = Math.abs(replies.get(freeRooms).getMeeting().getTemperature() - targetTemperature);
            if (forecastedDiff < minDiff){
                candidate = freeRooms;
                minDiff = forecastedDiff;
            }
        }
        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.addReceiver(candidate);
        msg.setConversationId(meeting.getId());
        msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
        msg.setOntology(MeetingOntology.getInstance().getName());
        try {
            myAgent.getContentManager().fillContent(msg, new Action(myAgent.getAID(), replies.get(candidate)));
        } catch (Exception e){e.printStackTrace();}
        context.getLogger().log("Accept proposal for meeting " + meeting.getId());
        myAgent.send(msg);
    }
}
