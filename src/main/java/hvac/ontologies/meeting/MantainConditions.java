package hvac.ontologies.meeting;

import jade.content.AgentAction;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

public class MantainConditions implements AgentAction {
    private List conditions;

    public MantainConditions() {
        conditions = new ArrayList();
    }

    public MantainConditions(List conditions) {
        this.conditions = conditions;
    }

    public List getConditions() {
        return conditions;
    }

    public void setConditions(List conditions) {
        this.conditions = conditions;
    }
}
