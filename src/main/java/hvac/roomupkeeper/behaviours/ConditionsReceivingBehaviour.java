package hvac.roomupkeeper.behaviours;

import com.google.common.base.Joiner;
import hvac.ontologies.meeting.MantainConditions;
import hvac.ontologies.meeting.Meeting;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.roomupkeeper.RoomUpkeeperContext;
import hvac.util.Conversions;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Comparator;
import java.util.List;

public class ConditionsReceivingBehaviour extends CyclicBehaviour {
    private final RoomUpkeeperContext context;
    private final MessageTemplate messageTemplate = MessageTemplate.and(
            MessageTemplate.MatchOntology(MeetingOntology.getInstance().getName()),
            MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0)
    );

    public ConditionsReceivingBehaviour(Agent a, RoomUpkeeperContext context) {
        super(a);
        this.context = context;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(messageTemplate);
        if(msg != null) {
            if(msg.getPerformative() == ACLMessage.REQUEST) {
                try {
                    processRequest(msg);
                } catch (Codec.CodecException | OntologyException e) {
                    context.getLogger().log("message not understood: "
                            + msg + ", exception: " + e.getMessage());
                    replyNotUnderstood(msg);
                }
            } else {
                context.getLogger().log("message not understood: "
                        + msg + ", not a request");
                replyNotUnderstood(msg);
            }
        } else {
            block();
        }
    }

    private void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
        ContentElement ce = myAgent.getContentManager().extractContent(msg);
        if (ce instanceof Action) {
            Concept action = ((Action) ce).getAction();
            if (action instanceof MantainConditions) {
                MantainConditions mantainConditions = (MantainConditions)action;
                List<Meeting> conditions = Conversions.toJavaList(mantainConditions.getConditions());
                conditions.sort(Comparator.comparing(Meeting::getStartDate));
                for(int i = 0; i <conditions.size()-1; i++) {
                    if(Conversions.toLocalDateTime(conditions.get(i).getEndDate())
                            .isAfter(Conversions.toLocalDateTime(conditions.get(i+1).getStartDate()))) {
                        replyRefuse(msg);
                        context.getLogger().log("refused to mantain conditions: "
                                + Joiner.on(", ").join(conditions)+", periods not mutually exclusive");
                        return;
                    }
                }
                replyAgree(msg);
                context.getLogger().log("agreed to mantain conditions: "
                        + Joiner.on(", ").join(conditions));
                context.setMeetingQueue(conditions);
                return;
            }
        }
        context.getLogger().log("message not understood: " + msg);
        replyNotUnderstood(msg);
    }

    private void replyAgree(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        myAgent.send(reply);
    }

    private void replyRefuse(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.REFUSE);
        myAgent.send(reply);
    }

    private void replyNotUnderstood(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        myAgent.send(reply);
    }
}
