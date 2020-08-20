package hvac.simulation;

import hvac.ontologies.roomclimate.InfoRequest;
import hvac.ontologies.roomclimate.RoomClimate;
import hvac.ontologies.weather.WeatherOntology;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

/**
 * class for use in agents that want communicate with simulation agent
 */
public class SimulationAgentMessenger {
    /**
     * prepares message that when sent to simulation agent will result in correct room climate
     * Requires that agent has registered sl0 language and room climate ontology
     * @param request request to send to simulation agent
     * @param myAgent agent that will send the message
     * @param simulationAgent simulation agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException
     * @throws OntologyException
     */
    public static ACLMessage prepareInfoRequest(InfoRequest request, Agent myAgent, AID simulationAgent)
            throws Codec.CodecException, OntologyException {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(simulationAgent);
        msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
        msg.setOntology(WeatherOntology.getInstance().getName());
        Action action = new Action(simulationAgent, request);
        myAgent.getContentManager().fillContent(msg, action);
        return msg;
    }


    /**
     * Extracts room climate from message received from simulation agent
     * Requires that agent has registered sl0 language and room climate ontology
     * @param myAgent calling agent
     * @param msg message with room climate
     * @return extracted room climate if message contains one
     * @throws Codec.CodecException
     * @throws OntologyException
     */
    public static Optional<RoomClimate> extractRoomClimate(Agent myAgent, ACLMessage msg)
            throws Codec.CodecException, OntologyException {
        ContentElement content = myAgent.getContentManager().extractContent(msg);
        if(content instanceof RoomClimate) {
            return Optional.of((RoomClimate)content);
        }
        return Optional.empty();
    }
}
