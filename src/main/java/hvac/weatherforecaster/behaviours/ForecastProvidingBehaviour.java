package hvac.weatherforecaster.behaviours;

import hvac.database.entities.WeatherSnapshot;
import hvac.ontologies.weather.Forecast;
import hvac.ontologies.weather.ForecastRequest;
import hvac.ontologies.weather.WeatherOntology;
import hvac.util.Conversions;
import hvac.util.Helpers;
import hvac.util.JadeCollectors;
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

import java.time.LocalDateTime;
import java.util.List;

public class ForecastProvidingBehaviour extends CyclicBehaviour {
    private final List<WeatherSnapshot> snapshots;
    private final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.and(
                    MessageTemplate.MatchOntology(WeatherOntology.getInstance().getName()),
                    MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0)
            )
    );

    public ForecastProvidingBehaviour(Agent agent, List<WeatherSnapshot> snapshots) {
        super(agent);
        this.snapshots = snapshots;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            try {
                processRequest(msg);
            } catch (OntologyException | Codec.CodecException e) {
                e.printStackTrace();
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
            if (action instanceof ForecastRequest) {
                ForecastRequest request = (ForecastRequest) action;
                LocalDateTime from = Conversions.toLocalDateTime(request.getFrom());
                LocalDateTime to = Conversions.toLocalDateTime(request.getTo());
                Forecast responseForecast = new Forecast(
                        snapshots
                                .stream()
                                .filter(snapshot ->
                                        Helpers.isBetween(snapshot.getDate(), from, to))
                                .map(Conversions::toOntologySnapshot)
                                .collect(JadeCollectors.toLeapList()));
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                myAgent.getContentManager().fillContent(reply, responseForecast);
                myAgent.send(reply);
                return;
            }
        }
        replyNotUnderstood(msg);
    }

    private void replyNotUnderstood(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        myAgent.send(reply);
    }
}
