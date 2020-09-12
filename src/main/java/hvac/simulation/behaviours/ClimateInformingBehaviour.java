package hvac.simulation.behaviours;

import hvac.ontologies.roomclimate.InfoRequest;
import hvac.ontologies.roomclimate.RoomClimateOntology;
import hvac.simulation.SimulationContext;
import hvac.simulation.rooms.RoomClimate;
import hvac.util.Conversions;
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

import java.util.Hashtable;

import static hvac.util.SimpleReplies.replyNotUnderstood;
import static hvac.util.SimpleReplies.replyRefuse;

public class ClimateInformingBehaviour extends RequestProcessingBehaviour {
    private final SimulationContext context;
    private final MessageTemplate template = MessageTemplate.and(
                    MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0),
                    MessageTemplate.MatchOntology(RoomClimateOntology.getInstance().getName())
    );

    public ClimateInformingBehaviour(Agent a, SimulationContext context) {
        super(a, context.getLogger());
        this.context = context;
    }

    @Override
    protected void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
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
                    context.getLogger().log("replying to climate request: roomId: " + infoRequest.getRoomId() +
                            ", temperature: " + roomClimate.getTemperature() +
                            ", relative humidity: " + roomClimate.getRelativeHumidity());
                    myAgent.send(reply);
                } else {
                    replyRefuse(myAgent, msg);
                }
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
