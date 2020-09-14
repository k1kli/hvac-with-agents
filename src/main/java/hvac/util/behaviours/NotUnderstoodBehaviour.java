package hvac.util.behaviours;

import hvac.util.Logger;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static hvac.util.SimpleReplies.replyNotUnderstood;

public class NotUnderstoodBehaviour extends CyclicBehaviour {
    private final Logger logger;
    private final MessageTemplate template;
    public NotUnderstoodBehaviour(Agent a, Logger logger, MessageTemplate ... templatesToIgnore) {
        super(a);
        this.logger = logger;
        template = constructIgnoreTemplate(templatesToIgnore);
    }

    private MessageTemplate constructIgnoreTemplate(MessageTemplate[] templatesToIgnore) {
        MessageTemplate res = MessageTemplate.MatchAll();
        for (MessageTemplate templateToIgnore :
                templatesToIgnore) {
            res = MessageTemplate.and(res, MessageTemplate.not(templateToIgnore));
        }
        return res;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(template);
        if(msg != null) {
            if(msg.getPerformative() != ACLMessage.NOT_UNDERSTOOD) {
                logger.log("received unexpected message: " + msg);
                replyNotUnderstood(myAgent, msg);
            }
        } else {
            block();
        }
    }
}
