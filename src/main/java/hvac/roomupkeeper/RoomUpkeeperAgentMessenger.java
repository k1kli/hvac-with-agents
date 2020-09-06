package hvac.roomupkeeper;

import hvac.ontologies.meeting.MantainConditions;
import hvac.ontologies.meeting.Meeting;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.util.Conversions;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

import java.util.List;

import static hvac.util.CommonMessengerFunctions.createActionMessage;

/**
 * class for use in agents that want communicate with room upkeeper agent
 */
public class RoomUpkeeperAgentMessenger {
    /**
     * prepares message that when sent to room upkeeper agent will make him update his conditions
     * or refuse if periods in which to maintain conditions are not mutually exclusive
     * Requires that agent has registered sl0 language and meeting ontology
     * @param conditions list of conditions to mantain by upkeeper - they are meeting objects
     * which contain conditions to mantain and dates between which they are mantained
     * @param myAgent agent that will send the message
     * @param roomUpkeeperAgent room upkeeper agent to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException language fipa-sl0 is not registered
     * @throws OntologyException meeting ontology is not registered
     */
    public static ACLMessage prepareMantainConditions(List<Meeting> conditions, Agent myAgent, AID roomUpkeeperAgent)
            throws Codec.CodecException, OntologyException {
        MantainConditions mantainConditions = new MantainConditions(Conversions.toJadeList(conditions));
        return createActionMessage(myAgent, roomUpkeeperAgent, mantainConditions, MeetingOntology.getInstance());
    }
}
