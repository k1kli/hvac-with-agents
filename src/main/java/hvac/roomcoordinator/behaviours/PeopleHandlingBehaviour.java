package hvac.roomcoordinator.behaviours;

import hvac.ontologies.presence.*;
import hvac.roomcoordinator.RoomContext;
import hvac.util.behaviours.RequestProcessingBehaviour;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static hvac.util.SimpleReplies.*;

public class PeopleHandlingBehaviour extends RequestProcessingBehaviour {
    private final RoomContext context;
    private final UpkeeperManagingBehaviour upkeeperManagingBehaviour;
    private final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0),
            MessageTemplate.MatchOntology(PresenceOntology.getInstance().getName())
    );
    private final List<Presence> presences = new ArrayList<>();

    public PeopleHandlingBehaviour(Agent agent, RoomContext context, UpkeeperManagingBehaviour upkeeperManagingBehaviour) {
        super(agent, context.getLogger());
        this.context = context;
        this.upkeeperManagingBehaviour = upkeeperManagingBehaviour;
    }

    @Override
    protected void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
        ContentElement ce = myAgent.getContentManager().extractContent(msg);
        if (ce instanceof Action) {
            Concept action = ((Action) ce).getAction();
            if (action instanceof RequestCurrentPresences) {
                handleRequestCurrentPresences(msg);
                return;
            } else if (action instanceof RequestAddPresence) {
                RequestAddPresence requestAddPresence = (RequestAddPresence) action;
                handleRequestAddPresence(msg, requestAddPresence);
                context.getLogger().log(msg.toString());
                return;
            }
        }
        replyNotUnderstood(myAgent, msg);
    }

    private void handleRequestCurrentPresences(ACLMessage msg) throws Codec.CodecException, OntologyException {
        PresencesInfo info = new PresencesInfo(presences);
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        myAgent.getContentManager().fillContent(reply, info);
        myAgent.send(reply);
        context.getLogger().log("replied with presence info to " + msg.getSender());
    }

    private void handleRequestAddPresence(ACLMessage msg, RequestAddPresence requestAddPresence) {
        Presence presenceToAdd = requestAddPresence.getPresence();
        if(!presenceToAdd.isValid() || presenceToAdd.getRoomId() != context.getMyRoomId()) {
            context.getLogger().log("invalid presence to add " + msg.getContent());
            replyRefuse(myAgent, msg);
        }
        if (presences.size() == 0
                || presences.get(0).getSinceLocal().isAfter(presenceToAdd.getUntilLocal())) {
            presences.add(0, presenceToAdd);
            replyAgree(myAgent, msg);
        } else if (presences.get(presences.size() - 1).getUntilLocal().isBefore(presenceToAdd.getSinceLocal())) {
            presences.add(presenceToAdd);
            replyAgree(myAgent, msg);
        } else {
            List<Presence> newPresences = new ArrayList<>(presences);
            int i = 0;
            for (; i < newPresences.size(); i++) {
                if (newPresences.get(i).getSinceLocal().isAfter(presenceToAdd.getSinceLocal())) {
                    break;
                }
            }
            newPresences.add(i, presenceToAdd);
            if (arePresencesValid(newPresences)) {
                context.getLogger().log("found place for " + msg.getSender());
                presences.clear();
                presences.addAll(newPresences);
                replyAgree(myAgent, msg);
            } else {
                context.getLogger().log("failed to find place for " + msg.getSender());
                replyRefuse(myAgent, msg);
            }
        }
    }

    //checks if at any point there will be too many people in the room
    private boolean arePresencesValid(List<Presence> presenceList) {
        //TODO:replace with chairs count from coordinator
        int maxPresences = 3;
        LinkedList<Presence> concurrentPresences = new LinkedList<>();
        for (Presence presence :
                presenceList) {
            concurrentPresences.removeIf(it->it.getUntilLocal().isBefore(presence.getSinceLocal())
            || it.getUntilLocal().isEqual(presence.getSinceLocal()));
            while (concurrentPresences.size() > 0
                    && (concurrentPresences.getFirst().getUntilLocal().isBefore(presence.getSinceLocal())
                    || concurrentPresences.getFirst().getUntilLocal().isEqual(presence.getSinceLocal()))) {
                concurrentPresences.removeFirst();
            }
            concurrentPresences.add(presence);
            if (concurrentPresences.size() > maxPresences) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected MessageTemplate getTemplate() {
        return template;
    }

}
