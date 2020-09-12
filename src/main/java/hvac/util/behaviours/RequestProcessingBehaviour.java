package hvac.util.behaviours;

import hvac.util.Logger;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static hvac.util.SimpleReplies.replyNotUnderstood;

public abstract class RequestProcessingBehaviour extends CyclicBehaviour {
    private final Logger logger;

    public RequestProcessingBehaviour(Agent myAgent, Logger logger) {
        super(myAgent);

        this.logger = logger;
    }

    @Override
    public final void action() {
        ACLMessage msg = myAgent.receive(getTemplate());
        if(msg != null) {
            if(msg.getPerformative() == ACLMessage.REQUEST) {
                try {
                    processRequest(msg);
                } catch (Codec.CodecException | OntologyException e) {
                    logger.log("not understood (parse exception):" + msg);
                    e.printStackTrace();
                    replyNotUnderstood(myAgent, msg);
                }
            } else {
                logger.log("not understood (not a request):" + msg);
                replyNotUnderstood(myAgent, msg);
            }
        } else {
            block();
        }
    }

    protected abstract void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException;
    protected abstract MessageTemplate getTemplate();
}
