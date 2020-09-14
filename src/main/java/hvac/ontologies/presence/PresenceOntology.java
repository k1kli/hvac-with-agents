package hvac.ontologies.presence;
import jade.content.onto.BeanOntology;
import jade.content.onto.Ontology;

public class PresenceOntology extends BeanOntology {
    public static final String ONTOLOGY_NAME = "Presence-ontology";

    private static final Ontology theInstance = new PresenceOntology();

    public static Ontology getInstance() {return theInstance;}

    public PresenceOntology() {
        super(ONTOLOGY_NAME);
        try {
            add(Presence.class);
            add(PresencesInfo.class);
            add(RequestAddPresence.class);
            add(RequestCurrentPresences.class);
        }
        catch (Exception oe) {oe.printStackTrace();}
    }




}
