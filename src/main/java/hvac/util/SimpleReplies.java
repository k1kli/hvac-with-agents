package hvac.util;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class SimpleReplies {

    public static void replyAgree(Agent myAgent, ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        myAgent.send(reply);
    }

    public static void replyRefuse(Agent myAgent, ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.REFUSE);
        myAgent.send(reply);
    }

    public static void replyNotUnderstood(Agent myAgent, ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        myAgent.send(reply);
    }
}
