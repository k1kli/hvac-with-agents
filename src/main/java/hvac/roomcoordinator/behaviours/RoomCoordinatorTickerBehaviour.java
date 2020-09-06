package hvac.roomcoordinator.behaviours;

import hvac.ontologies.meeting.Meeting;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.meeting.Request;
import hvac.ontologies.meeting.RequestStatus;
import hvac.roomcoordinator.RoomContext;
import hvac.time.DateTimeSimulator;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.time.Duration;
import java.time.LocalDateTime;

public class RoomCoordinatorTickerBehaviour extends TickerBehaviour {
    private final RoomContext roomContext;

    public RoomCoordinatorTickerBehaviour(Agent a, long period, RoomContext roomContext) {
        super(a, period);
        this.roomContext = roomContext;
    }

    @Override
    protected void onTick() {
        LocalDateTime now = DateTimeSimulator.getCurrentDate();
        if (null == roomContext.getCurrentMeeting()){
            Meeting meeting = roomContext.checkMeetings(now);
            if (null != meeting){
                roomContext.setCurrentMeeting(meeting);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                Request request = new Request(roomContext.getCurrentMeeting(), RequestStatus.EXECUTE);
                sendUpdateToUpkeeper(msg, request);
                this.reset(Duration.between(meeting.getLocalEndDate(), now).toMillis());
            }
            else{
                if (null != roomContext.peekMeeting()){
                    this.reset(Duration.between(roomContext.peekMeeting().getLocalStartDate(), now).toMillis());
                }
                else{
                    this.reset(Long.MAX_VALUE);
                }
            }
        }
        else{
            if (roomContext.getCurrentMeeting().getLocalEndDate().compareTo(now) < 0){
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

    private void fillAndSend(ACLMessage msg, Request request){
        try {
            myAgent.getContentManager().fillContent(msg, request);
        } catch (Exception e){e.printStackTrace();}
        myAgent.send(msg);
    }

    private void sendUpdateToUpkeeper(ACLMessage msg, Request request){
        msg.setConversationId(roomContext.getCurrentMeeting().getMeetingID());
        msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
        msg.setOntology(MeetingOntology.getInstance().getName());
        msg.addReceiver(roomContext.getMyRoomUpkeeper());
        fillAndSend(msg, request);
    }
}
