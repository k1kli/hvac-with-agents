package hvac.util.df;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.function.Consumer;

/**
 * Asks Directory facilitator for agent with given service name
 * when it receives response it calls provided function and removes itself
 */
public class FindingBehaviour extends CyclicBehaviour {
    DFAgentDescription agentToFind;
    Consumer<DFAgentDescription> afterFound;
    boolean messageSent;
    public FindingBehaviour(Agent agent, String nameOfAgentToFind,
                        Consumer<DFAgentDescription> afterFound) {
        super(agent);
        this.agentToFind = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setName(nameOfAgentToFind);
        agentToFind.addServices(sd);
        this.afterFound = afterFound;
    }

    @Override
    public void action()
    {
        if(!messageSent) {
            sendMessage();
            messageSent = true;
        }
        ACLMessage msg =
                myAgent.receive(MessageTemplate.MatchSender(myAgent.getDefaultDF()));

        if (msg != null)
        {
            try {
                DFAgentDescription[] dfds =
                        DFService.decodeNotification(msg.getContent());
                if (dfds.length > 0) {
                    afterFound.accept(dfds[0]);
                    myAgent.removeBehaviour(this);
                }
            }
            catch (Exception ex) {/*just wait for another message*/}
        }
        block();

    }

    private void sendMessage() {
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults((long) 1);

        myAgent.send(DFService.createSubscriptionMessage(myAgent, myAgent.getDefaultDF(),
                agentToFind, sc));
    }
}
