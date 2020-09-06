package hvac.util;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

public class CommonMessengerFunctions {

    public static ACLMessage createActionMessage(Agent myAgent, AID receivingAgent, jade.content.AgentAction request,
                                                  Ontology ontologyInstance)
            throws Codec.CodecException, OntologyException {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(receivingAgent);
        msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
        msg.setOntology(ontologyInstance.getName());
        Action action = new Action(receivingAgent, request);
        myAgent.getContentManager().fillContent(msg, action);
        return msg;
    }
}
