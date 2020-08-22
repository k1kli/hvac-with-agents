package hvac.ontologies.machinery;

import jade.content.Concept;

public class Ventilator implements Concept {
    private MachineParameter airExchangedPerSecond;

    public Ventilator(MachineParameter airExchangedPerSecond) {
        this.airExchangedPerSecond = airExchangedPerSecond;
    }

    public Ventilator() {
    }

    public MachineParameter getAirExchangedPerSecond() {
        return airExchangedPerSecond;
    }

    public void setAirExchangedPerSecond(MachineParameter airExchangedPerSecond) {
        this.airExchangedPerSecond = airExchangedPerSecond;
    }
}
