package hvac.roomcoordinator;

import hvac.ontologies.presence.Presence;
import hvac.ontologies.presence.PresenceOntology;
import hvac.ontologies.presence.RequestAddPresence;
import hvac.ontologies.presence.RequestCurrentPresences;
import hvac.util.CommonMessengerFunctions;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
public class RoomCoordinatorAgentMessenger {
    /**
     * prepares message that when sent to room coordinator agent for non meeting room
     * will result in list of that room presences
     * Requires that agent has registered sl0 language and presence ontology
     * @param myAgent agent that will send the message
     * @param roomCoordinator room coordinator agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException language fipa-sl0 is not registered
     * @throws OntologyException presence ontology is not registered
     */
    public static ACLMessage prepareRequestCurrentPresences(Agent myAgent, AID roomCoordinator)
            throws Codec.CodecException, OntologyException {
        RequestCurrentPresences requestCurrentPresences = new RequestCurrentPresences();
        return CommonMessengerFunctions.createActionMessage(
                myAgent, roomCoordinator, requestCurrentPresences, PresenceOntology.getInstance());
    }
    /**
     * prepares message that when sent to room coordinator agent for non meeting room
     * will result in it being added to that agents presences - conditions will be maintained in that room for
     * that person in that time period
     * Won't work if person already registered that presence in this room and it that presence until is not in the past
     * or if there is not enough room for given time period - check that with RequestCurrentPresences
     * Requires that agent has registered sl0 language and presence ontology
     * @param startTime time when person wants to enter the room
     * @param endTime time when person wants to leave the room
     * @param roomId room of given room coordinator
     * @param myAgent agent that will send the message
     * @param roomCoordinator room coordinator agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException language fipa-sl0 is not registered
     * @throws OntologyException presence ontology is not registered
     */
    public static ACLMessage prepareRequestAddPresence(LocalDateTime startTime, LocalDateTime endTime,
                                                       int roomId, Agent myAgent, AID roomCoordinator)
            throws Codec.CodecException, OntologyException {
        RequestAddPresence requestAddPresence = new RequestAddPresence(
                new Presence(myAgent.getAID(), roomId, startTime, endTime)
        );
        return CommonMessengerFunctions.createActionMessage(
                myAgent, roomCoordinator, requestAddPresence, PresenceOntology.getInstance());
    }
    /**
     * prepares message that when sent to room coordinator agent for non meeting room
     * will result in it being added to that agents presences - conditions will be maintained in that room for
     * that person in that time period
     * Only start time is included - conditions will be maintained starting from given point in time
     * or if there is not enough room for given time period - check that with RequestCurrentPresences
     * Another message should be sent to set end time
     * Won't work if person already registered that presence in this room and it that presence until is not in the past
     * or if there is not enough room for given time period - check that with RequestCurrentPresences
     * Requires that agent has registered sl0 language and presence ontology
     * @param startTime time when person wants to enter the room
     * @param roomId room of given room coordinator
     * @param myAgent agent that will send the message
     * @param roomCoordinator room coordinator agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException language fipa-sl0 is not registered
     * @throws OntologyException presence ontology is not registered
     */
    public static ACLMessage prepareRequestAddPresenceOnlyStart(LocalDateTime startTime,
                                                       int roomId, Agent myAgent, AID roomCoordinator)
            throws Codec.CodecException, OntologyException {
        RequestAddPresence requestAddPresence = new RequestAddPresence(
                new Presence(myAgent.getAID(), roomId, startTime, null)
        );
        return CommonMessengerFunctions.createActionMessage(
                myAgent, roomCoordinator, requestAddPresence, PresenceOntology.getInstance());
    }
    /**
     * prepares message that when sent to room coordinator agent for non meeting room
     * will result in it being added to that agents presences - conditions will be maintained in that room for
     * that person in that time period
     * Only end time is included - requires sending message with only start time first and that start time should be
     * before this end time
     * Requires that agent has registered sl0 language and presence ontology
     * @param endTime time when person wants to leave the room
     * @param roomId room of given room coordinator
     * @param myAgent agent that will send the message
     * @param roomCoordinator room coordinator agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException language fipa-sl0 is not registered
     * @throws OntologyException presence ontology is not registered
     */
    public static ACLMessage prepareRequestAddPresenceOnlyEnd(LocalDateTime endTime,
                                                                int roomId, Agent myAgent, AID roomCoordinator)
            throws Codec.CodecException, OntologyException {
        RequestAddPresence requestAddPresence = new RequestAddPresence(
                new Presence(myAgent.getAID(), roomId, null, endTime)
        );
        return CommonMessengerFunctions.createActionMessage(
                myAgent, roomCoordinator, requestAddPresence, PresenceOntology.getInstance());
    }
}
