package hvac.roomcoordinator.behaviours;

import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.meeting.Request;
import hvac.ontologies.meeting.RequestStatus;
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

public class ConditionsInformingBehaviour extends RequestProcessingBehaviour {
    private final RoomContext context;
    private final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.and(
                    MessageTemplate.MatchOntology(MeetingOntology.getInstance().getName()),
                    MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0)
            ),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
    );
    public ConditionsInformingBehaviour(Agent myAgent, RoomContext context) {
        super(myAgent, context.getLogger());
        this.context = context;
    }

    @Override
    protected void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
        ContentElement ce = myAgent.getContentManager().extractContent(msg);
        if (ce instanceof Action) {
            Concept action = ((Action) ce).getAction();
            if (action instanceof Request) {
                Request request = (Request) action;
                request.getMeeting().setTemperature(context.estimateTemperatureForNeighbour(request.getMeeting()));
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                request.setStatus(RequestStatus.ESTIMATION);
                myAgent.getContentManager().fillContent(reply, new Action(myAgent.getAID(), request));
                myAgent.send(reply);
                return;
            }
        }
        replyNotUnderstood(myAgent, msg);
    }

    @Override
    protected MessageTemplate getTemplate() {
        return template;
    }
}
