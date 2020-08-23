package hvac.ontologies.machinery;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.*;

public class MachineryOntology extends Ontology {
    public static final String ONTOLOGY_NAME = "Machinery-ontology";

    public static final String AIR_CONDITIONER = "AirConditioner";
    public static final String AIR_CONDITIONER_COOLING_POWER = "coolingPower";
    public static final String AIR_CONDITIONER_AIR_EXCHANGED_PER_SECOND = "airExchangedPerSecond";

    public static final String HEATER = "Heater";
    public static final String HEATER_HEATING_POWER = "heatingPower";

    public static final String VENTILATOR = "Ventilator";
    public static final String VENTILATOR_AIR_EXCHANGED_PER_SECOND = "airExchangedPerSecond";

    public static final String MACHINE_PARAMETER = "MachineParameter";
    public static final String MACHINE_PARAMETER_MAX_VALUE = "maxValue";
    public static final String MACHINE_PARAMETER_CURRENT_VALUE = "currentValue";

    public static final String MACHINERY = "Machinery";
    public static final String MACHINERY_AIR_CONDITIONER = "airConditioner";
    public static final String MACHINERY_HEATER = "heater";
    public static final String MACHINERY_VENTILATOR = "ventilator";

    public static final String UPDATE_MACHINERY = "UpdateMachinery";
    public static final String UPDATE_MACHINERY_MACHINERY = "machinery";
    public static final String UPDATE_MACHINERY_ROOM_ID = "roomId";

    public static final String REPORT_MACHINERY_STATUS = "ReportMachineryStatus";
    public static final String REPORT_MACHINERY_STATUS_ROOM_ID = "roomId";

    public static final String MACHINERY_STATUS = "MachineryStatus";
    public static final String MACHINERY_STATUS_MACHINERY = "machinery";
    public static final String MACHINERY_STATUS_ROOM_ID = "roomId";

    private static final Ontology theInstance = new MachineryOntology();

    public static Ontology getInstance() {return theInstance;}
    private MachineryOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        try {
            ConceptSchema cs = new ConceptSchema(MACHINE_PARAMETER);
                add(cs, MachineParameter.class);
            cs.add(MACHINE_PARAMETER_MAX_VALUE, (PrimitiveSchema)getSchema(BasicOntology.FLOAT), ObjectSchema.OPTIONAL);
            cs.add(MACHINE_PARAMETER_CURRENT_VALUE, (PrimitiveSchema)getSchema(BasicOntology.FLOAT));

            cs = new ConceptSchema(AIR_CONDITIONER);
            add(cs, AirConditioner.class);
            cs.add(AIR_CONDITIONER_AIR_EXCHANGED_PER_SECOND, (ConceptSchema)getSchema(MACHINE_PARAMETER),
                    ObjectSchema.OPTIONAL);
            cs.add(AIR_CONDITIONER_COOLING_POWER, (ConceptSchema)getSchema(MACHINE_PARAMETER),
                    ObjectSchema.OPTIONAL);

            cs = new ConceptSchema(HEATER);
            add(cs, Heater.class);
            cs.add(HEATER_HEATING_POWER, (ConceptSchema) getSchema(MACHINE_PARAMETER),
                    ObjectSchema.OPTIONAL);

            cs = new ConceptSchema(VENTILATOR);
            add(cs, Ventilator.class);
            cs.add(VENTILATOR_AIR_EXCHANGED_PER_SECOND, (ConceptSchema)getSchema(MACHINE_PARAMETER),
                    ObjectSchema.OPTIONAL);

            cs = new ConceptSchema(MACHINERY);
            add(cs, Machinery.class);
            cs.add(MACHINERY_AIR_CONDITIONER, (ConceptSchema)getSchema(AIR_CONDITIONER), ObjectSchema.OPTIONAL);
            cs.add(MACHINERY_HEATER, (ConceptSchema)getSchema(HEATER), ObjectSchema.OPTIONAL);
            cs.add(MACHINERY_VENTILATOR, (ConceptSchema)getSchema(VENTILATOR), ObjectSchema.OPTIONAL);

            AgentActionSchema as = new AgentActionSchema(UPDATE_MACHINERY);
            add(as, UpdateMachinery.class);
            as.add(UPDATE_MACHINERY_MACHINERY, (ConceptSchema)getSchema(MACHINERY));
            as.add(UPDATE_MACHINERY_ROOM_ID, (PrimitiveSchema)getSchema(BasicOntology.INTEGER));

            PredicateSchema ps = new PredicateSchema(MACHINERY_STATUS);
            add(ps, MachineryStatus.class);
            ps.add(MACHINERY_STATUS_MACHINERY, getSchema(MACHINERY));
            ps.add(MACHINERY_STATUS_ROOM_ID, getSchema(BasicOntology.INTEGER));

            as = new AgentActionSchema(REPORT_MACHINERY_STATUS);
            add(as, ReportMachineryStatus.class);
            as.add(REPORT_MACHINERY_STATUS_ROOM_ID, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }
}
