package hvac.simulation.behaviours;

import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.meeting.Request;
import hvac.simulation.SimulationContext;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgentfullModeHandlingBehaviour extends CyclicBehaviour {
    private final SimulationContext context;
    private final MessageTemplate messageTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.and(
                    MessageTemplate.MatchOntology(MeetingOntology.getInstance().getName()),
                    MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0)));

    public AgentfullModeHandlingBehaviour(Agent agent, SimulationContext context) {
        super(agent);
        this.context = context;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(messageTemplate);
        if (msg != null) {
            extractRequest(msg);
        } else {
            block();
        }
    }

    private void extractRequest(ACLMessage msg) {
        Request request = null;
        try {
            request = (Request) ((Action) myAgent.getContentManager().extractContent(msg)).getAction();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null == request || null == request.getMeeting()) {
            notUnderstood(msg);
            return;
        }
        context.getCalendarMeetings().add(request.getMeeting());
    }

    private void notUnderstood(ACLMessage msg) {
        context.getLogger().log("Not understood " + msg);
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        reply.setContent(msg.getContent());
        myAgent.send(reply);
    }
}

