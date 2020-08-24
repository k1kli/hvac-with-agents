package hvac.ontologies.machinery;

import jade.content.Concept;

public class Heater implements Concept {
    private MachineParameter heatingPower;

    public Heater(MachineParameter heatingPower) {
        this.heatingPower = heatingPower;
    }

    public Heater() {
    }

    public MachineParameter getHeatingPower() {
        return heatingPower;
    }

    public void setHeatingPower(MachineParameter heatingPower) {
        this.heatingPower = heatingPower;
    }
}
