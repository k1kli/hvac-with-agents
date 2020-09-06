package hvac.ontologies.meeting;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PrimitiveSchema;

public class MeetingOntology extends Ontology {

    public static final String ONTOLOGY_NAME = "Meeting-ontology";

    public static final String MEETING = "meeting";
    public static final String MEETING_ID = "meetingID";
    public static final String MEETING_START_DATE = "startDate";
    public static final String MEETING_END_DATE = "endDate";
    public static final String MEETING_PEOPLE = "peopleInRoom";
    public static final String MEETING_TEMPERATURE = "temperature";

    public static final String REQUEST = "Request";
    public static final String REQUEST_MEETING = "meeting";
    public static final String REQUEST_STATUS = "requestStatus";

    private static final Ontology theInstance = new MeetingOntology();

    public static Ontology getInstance() {return theInstance;}

    private MeetingOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());
        try {
            ConceptSchema cs = new ConceptSchema(MEETING);
            add(cs, Meeting.class);
            cs.add(MEETING_ID, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            cs.add(MEETING_START_DATE, (PrimitiveSchema) getSchema(BasicOntology.DATE));
            cs.add(MEETING_END_DATE, (PrimitiveSchema) getSchema(BasicOntology.DATE));
            cs.add(MEETING_PEOPLE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            cs.add(MEETING_TEMPERATURE, (PrimitiveSchema) getSchema(BasicOntology.FLOAT));

            AgentActionSchema as = new AgentActionSchema(REQUEST);
            add(as, Request.class);
            as.add(REQUEST_MEETING, (ConceptSchema) getSchema(MEETING));
            //as.add(REQUEST_STATUS, (ConceptSchema) getSchema(REQUEST_STATUS));
            as.add(REQUEST_STATUS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
        }
        catch (Exception oe) {oe.printStackTrace();}
    }
}

