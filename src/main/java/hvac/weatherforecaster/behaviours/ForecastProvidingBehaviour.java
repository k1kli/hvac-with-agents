package hvac.weatherforecaster.behaviours;

import hvac.database.entities.WeatherSnapshot;
import hvac.ontologies.weather.Forecast;
import hvac.ontologies.weather.ForecastRequest;
import hvac.ontologies.weather.WeatherOntology;
import hvac.util.Conversions;
import hvac.util.Helpers;
import hvac.util.JadeCollectors;
import hvac.util.Logger;
import hvac.util.behaviours.RequestProcessingBehaviour;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static hvac.util.SimpleReplies.replyNotUnderstood;

public class ForecastProvidingBehaviour extends RequestProcessingBehaviour {
    private final List<WeatherSnapshot> snapshots;
    private final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
            MessageTemplate.and(
                    MessageTemplate.MatchOntology(WeatherOntology.getInstance().getName()),
                    MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0)
            )
    );

    public ForecastProvidingBehaviour(Agent agent, List<WeatherSnapshot> snapshots, Logger logger) {
        super(agent, logger);
        this.snapshots = snapshots;
    }

    @Override
    protected void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
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
        replyNotUnderstood(myAgent, msg);
    }

    @Override
    protected MessageTemplate getTemplate() {
        return template;
    }
}
