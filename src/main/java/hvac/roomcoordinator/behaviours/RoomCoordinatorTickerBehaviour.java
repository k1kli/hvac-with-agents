package hvac.roomcoordinator.behaviours;

import com.google.common.base.Joiner;
import hvac.ontologies.meeting.Meeting;
import hvac.roomcoordinator.RoomContext;
import hvac.roomupkeeper.RoomUpkeeperAgentMessenger;
import hvac.time.DateTimeSimulator;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomCoordinatorTickerBehaviour extends TickerBehaviour {
    private final RoomContext roomContext;
    private List<Meeting> pendingMeetingsInUpkeeper = new ArrayList<>();
    private List<Meeting> meetingsInUpkeeper = new ArrayList<>();
    private final MessageTemplate upkeeperTemplate;
    private final long standardPeriod;
    private Step step = Step.CHECKING_MEETING_CHANGES;
    private enum Step {
        CHECKING_MEETING_CHANGES,
        AWAITING_UPKEEPER_RESPONSE
    }

    public RoomCoordinatorTickerBehaviour(Agent a, long period, RoomContext roomContext) {
        super(a, period);
        this.roomContext = roomContext;
        upkeeperTemplate = MessageTemplate.MatchSender(roomContext.getMyRoomUpkeeper());
        standardPeriod = period;
    }

    @Override
    protected void onTick() {
        switch (step) {

            case CHECKING_MEETING_CHANGES:
                checkMeetingChanges();
                break;
            case AWAITING_UPKEEPER_RESPONSE:
                awaitUpkeeperResponse();
                break;
        }

    }

    private void checkMeetingChanges() {
        List<Meeting> incomingMeetings =
                roomContext.getMeetingsStartingBefore(DateTimeSimulator.getCurrentDate().plusMinutes(30));
        if(!meetingsInUpkeeper.equals(incomingMeetings)) {
            try {
                ACLMessage msg = RoomUpkeeperAgentMessenger.prepareMantainConditions(incomingMeetings, myAgent, roomContext.getMyRoomUpkeeper());
                myAgent.send(msg);
                step = Step.AWAITING_UPKEEPER_RESPONSE;
                pendingMeetingsInUpkeeper = incomingMeetings;
                reset(1);
            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
            }
        } else {
            reset(standardPeriod);
        }
    }

    private void awaitUpkeeperResponse() {
        ACLMessage msg = myAgent.receive(upkeeperTemplate);
        if(msg != null) {
            if (msg.getPerformative() == ACLMessage.AGREE) {
                meetingsInUpkeeper = pendingMeetingsInUpkeeper;
            } else {
                roomContext.getLogger().log("upkeeper hasn't started maintaining conditions, conditions: "
                + Joiner.on(",").join(pendingMeetingsInUpkeeper.stream().map(meeting->
                    "startDate="+meeting.getStartDate()+
                            ", endDate="+meeting.getEndDate()+
                            ", people"+meeting.getPeopleInRoom()+
                            ", meetingId"+meeting.getMeetingID()+
                            ", temperature"+meeting.getTemperature())
                        .collect(Collectors.toList())
                ));
            }
            step = Step.CHECKING_MEETING_CHANGES;
            reset(standardPeriod);
        }
    }
}
