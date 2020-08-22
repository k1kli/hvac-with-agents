package hvac.ontologies.machinery;

import jade.content.Predicate;

public class MachineryStatus implements Predicate {
    private Machinery machinery;

    public MachineryStatus(Machinery machinery) {
        this.machinery = machinery;
    }

    public MachineryStatus() {
    }

    public Machinery getMachinery() {
        return machinery;
    }

    public void setMachinery(Machinery machinery) {
        this.machinery = machinery;
    }
}
