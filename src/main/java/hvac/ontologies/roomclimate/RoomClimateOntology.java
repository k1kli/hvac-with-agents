package hvac.ontologies.roomclimate;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

public class RoomClimateOntology extends Ontology {
    public static final String ONTOLOGY_NAME = "Room-climate-ontology";

    public static final String ROOM_CLIMATE = "RoomClimate";
    public static final String ROOM_CLIMATE_ROOM_ID = "roomId";
    public static final String ROOM_CLIMATE_TEMPERATURE = "temperature";
    public static final String ROOM_CLIMATE_ABSOLUTE_HUMIDITY = "absoluteHumidity";
    public static final String ROOM_CLIMATE_RELATIVE_HUMIDITY = "relativeHumidity";
    public static final String ROOM_CLIMATE_AIR_QUALITY = "airQuality";

    public static final String INFO_REQUEST = "InfoRequest";
    public static final String INFO_REQUEST_ROOM_ID = "roomId";

    private static final Ontology theInstance = new RoomClimateOntology();

    public static Ontology getInstance() {return theInstance;}

    private RoomClimateOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        try {
            PredicateSchema ps = new PredicateSchema(ROOM_CLIMATE);
            add(ps, RoomClimate.class);
            ps.add(ROOM_CLIMATE_ROOM_ID, getSchema(BasicOntology.INTEGER));
            ps.add(ROOM_CLIMATE_TEMPERATURE, getSchema(BasicOntology.FLOAT));
            ps.add(ROOM_CLIMATE_ABSOLUTE_HUMIDITY, getSchema(BasicOntology.FLOAT));
            ps.add(ROOM_CLIMATE_RELATIVE_HUMIDITY, getSchema(BasicOntology.FLOAT));
            ps.add(ROOM_CLIMATE_AIR_QUALITY, getSchema(BasicOntology.FLOAT));

            AgentActionSchema as = new AgentActionSchema(INFO_REQUEST);
            add(as, InfoRequest.class);
            as.add(INFO_REQUEST_ROOM_ID, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }
}
