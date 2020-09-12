package hvac.roomcoordinator.behaviours;

import hvac.ontologies.presence.PresenceOntology;
import hvac.ontologies.presence.RequestAddPresence;
import hvac.ontologies.presence.RequestCurrentPresences;
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

import static hvac.util.SimpleReplies.replyNotUnderstood;

public class PeopleHandlingBehaviour extends RequestProcessingBehaviour {
    private final RoomContext context;
    private final UpkeeperManagingBehaviour upkeeperManagingBehaviour;
    private final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0),
            MessageTemplate.MatchOntology(PresenceOntology.getInstance().getName())
    );

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
                RequestCurrentPresences requestCurrentPresences = (RequestCurrentPresences) action;
                context.getLogger().log(msg.toString());
                return;
            } else if (action instanceof RequestAddPresence) {
                RequestAddPresence requestAddPresence = (RequestAddPresence) action;
                context.getLogger().log(msg.toString());
            }
        }
        replyNotUnderstood(myAgent, msg);
    }

    @Override
    protected MessageTemplate getTemplate() {
        return template;
    }

}
