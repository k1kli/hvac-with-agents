package hvac.weatherforecaster;

import hvac.ontologies.weather.Forecast;
import hvac.ontologies.weather.ForecastRequest;
import hvac.ontologies.weather.WeatherOntology;
import hvac.util.Conversions;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * class for use in agents that want to ask forecaster for weather forecast
 */
public class WeatherForecasterMessenger {
    /**
     * prepares message that when sent to Weather forecaster will result in correct snapshot
     * Requires that agent has registered sl0 language and weather ontology
     * @param from oldest date that provided forecast can have
     * @param to newest date that provided forecasts can have
     * @param myAgent agent that will send the message
     * @param forecaster forecaster to which message will be sent
     * @return prepared message
     * @throws Codec.CodecException
     * @throws OntologyException
     */
    public static ACLMessage prepareForecastRequest(LocalDateTime from, LocalDateTime to, Agent myAgent, AID forecaster)
            throws Codec.CodecException, OntologyException {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(forecaster);
        msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
        msg.setOntology(WeatherOntology.getInstance().getName());
        Action action = new Action(forecaster, new ForecastRequest(
                Conversions.toDate(from),
                Conversions.toDate(to)
        ));
        myAgent.getContentManager().fillContent(msg, action);
        return msg;
    }

    /**
     * Extracts forecast from message received from weather forecaster
     * Requires that agent has registered sl0 language and weather ontology
     * @param myAgent calling agent
     * @param msg message with forecast
     * @return extracted forecast if message contains one
     * @throws Codec.CodecException
     * @throws OntologyException
     */
    public static Optional<Forecast> extractForecast(Agent myAgent, ACLMessage msg)
            throws Codec.CodecException, OntologyException {
        ContentElement content = myAgent.getContentManager().extractContent(msg);
        if(content instanceof Forecast) {
            return Optional.of((Forecast)content);
        }
        return Optional.empty();
    }
}
