package hvac.simulation;

import hvac.ontologies.machinery.Machinery;
import hvac.ontologies.machinery.MachineryOntology;
import hvac.ontologies.machinery.ReportMachineryStatus;
import hvac.ontologies.machinery.UpdateMachinery;
import hvac.ontologies.roomclimate.InfoRequest;
import hvac.ontologies.roomclimate.RoomClimate;
import hvac.ontologies.weather.WeatherOntology;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
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
@SuppressWarnings("unused")
public class SimulationAgentMessenger {
    /**
     * prepares message that when sent to simulation agent will result in correct room climate
     * Requires that agent has registered sl0 language and room climate ontology
     * @param roomId roomId for which climate will be requested
     * @param myAgent agent that will send the message
     * @param simulationAgent simulation agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException language fipa-sl0 is not registered
     * @throws OntologyException weather ontology is not registered
     */
    public static ACLMessage prepareInfoRequest(int roomId, Agent myAgent, AID simulationAgent)
            throws Codec.CodecException, OntologyException {
        InfoRequest request = new InfoRequest(roomId);
        return createActionMessage(myAgent, simulationAgent, request, WeatherOntology.getInstance());
    }


    /**
     * Extracts room climate from message received from simulation agent
     * Requires that agent has registered sl0 language and room climate ontology
     * @param myAgent calling agent
     * @param msg message with room climate
     * @return extracted room climate if message contains one
     * @throws Codec.CodecException language fipa-sl0 is not registered or message received is not in this language
     * @throws OntologyException weather ontology is not registered or message received is not in this ontology
     */
    public static Optional<RoomClimate> extractRoomClimate(Agent myAgent, ACLMessage msg)
            throws Codec.CodecException, OntologyException {
        ContentElement content = myAgent.getContentManager().extractContent(msg);
        if(content instanceof RoomClimate) {
            return Optional.of((RoomClimate)content);
        }
        return Optional.empty();
    }

    /**
     * prepares message that when sent to simulation agent will result in machinery info provided
     * Requires that agent has registered sl0 language and machinery ontology
     * @param roomId room for which to request info
     * @param myAgent agent that will send the message
     * @param simulationAgent simulation agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException language fipa-sl0 is not registered
     * @throws OntologyException weather ontology is not registered
     */
    public static ACLMessage prepareReportMachineryStatus(int roomId, Agent myAgent, AID simulationAgent)
            throws Codec.CodecException, OntologyException {
        ReportMachineryStatus request = new ReportMachineryStatus(roomId);
        return createActionMessage(myAgent, simulationAgent, request, MachineryOntology.getInstance());
    }


    /**
     * Extracts machinery status from message received from simulation agent
     * Requires that agent has registered sl0 language and room climate ontology
     * @param myAgent calling agent
     * @param msg message with room climate
     * @return extracted machinery status if message contains one
     * @throws Codec.CodecException language fipa-sl0 is not registered or message received is not in this language
     * @throws OntologyException weather ontology is not registered or message received is not in this ontology
     */
    public static Optional<RoomClimate> extractMachineryStatus(Agent myAgent, ACLMessage msg)
            throws Codec.CodecException, OntologyException {
        ContentElement content = myAgent.getContentManager().extractContent(msg);
        if(content instanceof RoomClimate) {
            return Optional.of((RoomClimate)content);
        }
        return Optional.empty();
    }

    /**
     * prepares message that when sent to simulation agent will result in machinery parameters updated
     * Requires that agent has registered sl0 language and machinery ontology
     * @param machinery updated machinery parameters (in parameter::currentValue)
     * @param myAgent agent that will send the message
     * @param simulationAgent simulation agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException language fipa-sl0 is not registered
     * @throws OntologyException weather ontology is not registered
     */
    public static ACLMessage prepareUpdateMachinery(Machinery machinery, int roomId, Agent myAgent, AID simulationAgent)
            throws Codec.CodecException, OntologyException {
        UpdateMachinery request = new UpdateMachinery(machinery, roomId);
        return createActionMessage(myAgent, simulationAgent, request, MachineryOntology.getInstance());
    }

    private static ACLMessage createActionMessage(Agent myAgent, AID simulationAgent, jade.content.AgentAction request,
                                                  Ontology ontologyInstance)
            throws Codec.CodecException, OntologyException {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(simulationAgent);
        msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
        msg.setOntology(ontologyInstance.getName());
        Action action = new Action(simulationAgent, request);
        myAgent.getContentManager().fillContent(msg, action);
        return msg;
    }
}
