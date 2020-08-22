package hvac.ontologies.machinery;

import jade.content.AgentAction;

public class UpdateMachinery implements AgentAction {
    private Machinery machinery;

    public UpdateMachinery(Machinery machinery) {
        this.machinery = machinery;
    }

    public UpdateMachinery() {
    }

    public Machinery getMachinery() {
        return machinery;
    }

    public void setMachinery(Machinery machinery) {
        this.machinery = machinery;
    }
}
