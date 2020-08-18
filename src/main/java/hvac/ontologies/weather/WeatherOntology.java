package hvac.ontologies.weather;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.*;

public class WeatherOntology extends Ontology {
    public static final String ONTOLOGY_NAME = "Weather-ontology";

    public static final String WEATHER_SNAPSHOT = "WeatherSnapshot";
    public static final String WEATHER_SNAPSHOT_TIME = "time";
    public static final String WEATHER_SNAPSHOT_TEMPERATURE = "temperature";
    public static final String WEATHER_SNAPSHOT_PRESSURE = "pressure";
    public static final String WEATHER_SNAPSHOT_ABSOLUTE_HUMIDITY = "absoluteHumidity";

    public static final String FORECAST = "Forecast";
    public static final String FORECAST_WEATHER_SNAPSHOTS = "weatherSnapshots";

    public static final String FORECAST_REQUEST = "ForecastRequest";
    public static final String FORECAST_REQUEST_FROM = "from";
    public static final String FORECAST_REQUEST_TO = "to";

    private static Ontology theInstance = new WeatherOntology();

    public static Ontology getInstance() {return theInstance;}

    private WeatherOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        try {
            AgentActionSchema as = new AgentActionSchema(FORECAST_REQUEST);
            add(as, ForecastRequest.class);
            as.add(FORECAST_REQUEST_FROM, (PrimitiveSchema) getSchema(BasicOntology.DATE));
            as.add(FORECAST_REQUEST_TO, (PrimitiveSchema) getSchema(BasicOntology.DATE));

            ConceptSchema cs = new ConceptSchema(WEATHER_SNAPSHOT);
            add(cs, WeatherSnapshot.class);
            cs.add(WEATHER_SNAPSHOT_TIME, (PrimitiveSchema) getSchema(BasicOntology.DATE));
            cs.add(WEATHER_SNAPSHOT_TEMPERATURE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(WEATHER_SNAPSHOT_PRESSURE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            cs.add(WEATHER_SNAPSHOT_ABSOLUTE_HUMIDITY, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));

            PredicateSchema ps = new PredicateSchema(FORECAST);
            add(ps, Forecast.class);
            ps.add(FORECAST_WEATHER_SNAPSHOTS, (ConceptSchema)getSchema(WEATHER_SNAPSHOT),
                    0, ObjectSchema.UNLIMITED);

        }
        catch (Exception oe) {oe.printStackTrace();}
    }
}
