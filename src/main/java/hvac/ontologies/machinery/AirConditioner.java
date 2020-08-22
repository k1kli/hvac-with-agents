package hvac.ontologies.machinery;

import jade.content.Concept;

public class AirConditioner implements Concept {
    private MachineParameter airExchangedPerSecond;
    private MachineParameter coolingPower;

    public AirConditioner(MachineParameter airExchangedPerSecond, MachineParameter coolingPower) {
        this.airExchangedPerSecond = airExchangedPerSecond;
        this.coolingPower = coolingPower;
    }

    public AirConditioner() {
    }

    public MachineParameter getAirExchangedPerSecond() {
        return airExchangedPerSecond;
    }

    public void setAirExchangedPerSecond(MachineParameter airExchangedPerSecond) {
        this.airExchangedPerSecond = airExchangedPerSecond;
    }

    public MachineParameter getCoolingPower() {
        return coolingPower;
    }

    public void setCoolingPower(MachineParameter coolingPower) {
        this.coolingPower = coolingPower;
    }
}
