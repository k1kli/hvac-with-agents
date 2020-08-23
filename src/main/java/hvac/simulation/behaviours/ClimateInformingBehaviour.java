package hvac.simulation.behaviours;

import hvac.ontologies.roomclimate.InfoRequest;
import hvac.ontologies.roomclimate.RoomClimateOntology;
import hvac.simulation.SimulationContext;
import hvac.simulation.rooms.RoomClimate;
import hvac.util.Conversions;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class ClimateInformingBehaviour extends CyclicBehaviour {
    private final SimulationContext context;
    private final MessageTemplate messageTemplate = MessageTemplate.and(
                    MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0),
                    MessageTemplate.MatchOntology(RoomClimateOntology.getInstance().getName())
    );

    public ClimateInformingBehaviour(Agent a, SimulationContext context) {
        super(a);
        this.context = context;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(messageTemplate);
        if(msg != null) {
            if(msg.getPerformative() == ACLMessage.REQUEST) {
                try {
                    processRequest(msg);
                } catch (Codec.CodecException | OntologyException e) {
                    replyNotUnderstood(msg);
                }
            } else {
                replyNotUnderstood(msg);
            }
        } else {
            block();
        }
    }

    private void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
        ContentElement ce = myAgent.getContentManager().extractContent(msg);
        if (ce instanceof Action) {
            Concept action = ((Action) ce).getAction();
            if (action instanceof InfoRequest) {
                InfoRequest infoRequest = (InfoRequest)action;
                Hashtable<Integer, RoomClimate> climates = context.getClimates();
                if(climates.containsKey(infoRequest.getRoomId())) {
                    hvac.ontologies.roomclimate.RoomClimate roomClimate
                            = Conversions.toOntologyRoomClimate(
                                    infoRequest.getRoomId(),
                            climates.get(infoRequest.getRoomId()));
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    myAgent.getContentManager().fillContent(reply, roomClimate);
                    myAgent.send(reply);
                    return;
                }
            }
        }
        replyNotUnderstood(msg);
    }

    private void replyNotUnderstood(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        myAgent.send(reply);
    }
}
