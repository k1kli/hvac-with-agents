package hvac.weatherforecaster;

import hvac.database.Connection;
import hvac.database.entities.WeatherSnapshot;
import hvac.ontologies.weather.WeatherOntology;
import hvac.time.DateTimeSimulator;
import hvac.weather.DatabaseForecastProvider;
import hvac.weather.interfaces.ForecastProvider;
import hvac.weatherforecaster.behaviours.ForecastProvidingBehaviour;
import hvac.weatherforecaster.behaviours.WeatherGettingBehaviour;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.domain.FIPANames;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class WeatherForecasterAgent extends Agent {
    private final List<WeatherSnapshot> oneWeekPlusWeather = new ArrayList<>();

    @Override
    protected void setup() {
        if (getArguments() == null || getArguments().length != 2) {
            usage("Wrong args num");
            doDelete();
            return;
        }
        LocalDateTime startTime;
        float timeScale;
        try {
            timeScale = Float.parseFloat(getArguments()[0].toString());
            startTime = LocalDateTime.parse(getArguments()[1].toString(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (NumberFormatException | DateTimeParseException e) {
            usage(e.getMessage());
            doDelete();
            return;
        }
        DateTimeSimulator.init(startTime, timeScale);
        getContentManager().registerLanguage(new SLCodec(),
                FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(WeatherOntology.getInstance());
        Connection database = new Connection();
        ForecastProvider provider = new DatabaseForecastProvider(database);
        int weatherGettingSpeed = (int)(1000*3600/timeScale);
        addBehaviour(new WeatherGettingBehaviour(this, weatherGettingSpeed, provider, oneWeekPlusWeather));
        addBehaviour(new ForecastProvidingBehaviour(this, oneWeekPlusWeather));
    }

    private void usage(String err) {
        System.err.println("-------- Weather agent usage --------------");
        System.err.println("simulation:hvac.weatherforecaster.WeatherForecasterAgent(timeScale, start_date)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("err:" + err);
    }
}
