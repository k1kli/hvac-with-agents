package hvac.roomcoordinator.behaviours;

import hvac.ontologies.meeting.Request;
import hvac.ontologies.meeting.RequestStatus;
import hvac.ontologies.presence.PresenceOntology;
import hvac.roomcoordinator.RoomContext;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MeetingHandlingBehaviour extends CyclicBehaviour {
    private final RoomContext roomContext;
    private final UpkeeperManagingBehaviour tickerBehaviour;
    private final MessageTemplate messageTemplate;

    public MeetingHandlingBehaviour(Agent a, RoomContext roomContext, UpkeeperManagingBehaviour tickerBehaviour) {
        super(a);
        this.roomContext = roomContext;
        this.tickerBehaviour = tickerBehaviour;
        messageTemplate = MessageTemplate.and(
                //don't catch UpkeeperManagingBehaviour messages
                MessageTemplate.not(MessageTemplate.MatchSender(
                        roomContext.getMyRoomUpkeeper())
                ),
                MessageTemplate.and(
                        //don't catch PeopleHandlingBehaviour messages
                        MessageTemplate.not(MessageTemplate.MatchOntology(
                                PresenceOntology.getInstance().getName())
                        ),
                        //don't catch ConditionsInformingBehaviour messages
                        MessageTemplate.not(MessageTemplate.MatchPerformative(
                                ACLMessage.REQUEST))
                ));
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(messageTemplate);
        if (msg != null) {
            handleMessage(msg);
        } else {
            block();
        }
    }

    private void handleMessage(ACLMessage msg) {
        switch (msg.getPerformative()) {
            case ACLMessage.CFP:
                handleCFP(msg);
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

    private void handleCFP(ACLMessage msg) {
        Request request = extractRequest(msg);
        if (null == request) {
            return;
        }
        if (!roomContext.isTimeSlotAvailable(request.getMeeting())) {
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            request.setStatus(RequestStatus.FAILED);
            fillAndSend(reply, request);
            return;
        }
        ACLMessage requestForecast = msg.createReply();
        requestForecast.setPerformative(ACLMessage.REQUEST);
        requestForecast.setConversationId(requestForecast.getConversationId() + "-req-room-" + roomContext.getMyRoomId());
        requestForecast.clearAllReceiver();
        for (AID neighbour : roomContext.getMyNeighbours().keySet()) {
            requestForecast.addReceiver(neighbour);
        }
        request.setStatus(RequestStatus.FORECAST);
        roomContext.newForecastEntry(request.getMeeting());
        fillAndSend(requestForecast, request);
    }

    private void handleInform(ACLMessage msg) {
        Request request = extractRequest(msg);
        if (null == request) {
            return;
        }
        roomContext.addNeighbourForecast(
                request.getMeeting().getMeetingID(),
                msg.getSender(),
                request.getMeeting().getTemperature());
        if (!roomContext.isForecastCompleted(request.getMeeting().getMeetingID())) {
            return;
        }
        request.getMeeting().setTemperature(roomContext.computeForecast(request.getMeeting().getMeetingID()));
        roomContext.removeForecastEntry(request.getMeeting().getMeetingID());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        reply.setConversationId(request.getMeeting().getMeetingID());
        reply.clearAllReceiver();
        reply.addReceiver(roomContext.getCoordinator());
        request.setStatus(RequestStatus.OFFER);
        fillAndSend(reply, request);
    }

    private void handleAcceptProposal(ACLMessage msg) {
        Request request = extractRequest(msg);
        if (null == request) {
            return;
        }
        ACLMessage reply = msg.createReply();
        if (roomContext.isTimeSlotAvailable(request.getMeeting())) {
            reply.setPerformative(ACLMessage.INFORM);
            request.setStatus(RequestStatus.RESERVED);
            roomContext.addMeeting(request.getMeeting());
        } else {
            reply.setPerformative(ACLMessage.FAILURE);
            request.setStatus(RequestStatus.FAILED);
        }
        fillAndSend(reply, request);
        tickerBehaviour.reset(1);
    }

    private void handleCancel(ACLMessage msg) {
        Request request = extractRequest(msg);
        if (null == request) {
            return;
        }
        roomContext.removeMeeting(request.getMeeting().getMeetingID());
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.CONFIRM);
        request.setStatus(RequestStatus.CANCELLED);
        fillAndSend(reply, request);
        tickerBehaviour.reset(1);
    }

    private void notUnderstood(ACLMessage msg) {
        roomContext.getLogger().log("Not understood " + msg);
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        reply.setContent(msg.getContent());
        myAgent.send(reply);
    }

    private Request extractRequest(ACLMessage msg) {
        Request request = null;
        try {
            request = (Request) ((Action) myAgent.getContentManager().extractContent(msg)).getAction();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == request || null == request.getMeeting()) {
            notUnderstood(msg);
            return null;
        }
        return request;
    }

    private void fillAndSend(ACLMessage msg, Request request) {
        try {
            myAgent.getContentManager().fillContent(msg, new Action(myAgent.getAID(), request));
        } catch (Exception e) {
            e.printStackTrace();
        }
        msg.clearAllReplyTo();
        msg.setInReplyTo("");
        msg.setReplyWith("");
        //roomContext.getLogger().log("Message sent: " + msg);
        myAgent.send(msg);
    }
}
