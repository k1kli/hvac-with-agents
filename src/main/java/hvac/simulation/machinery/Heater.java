package hvac.simulation.machinery;

public class Heater {
    //TODO: calculate energy used
    private final MachineParameter heatingPower;// in watts

    public Heater(MachineParameter heatingPower) {
        this.heatingPower = heatingPower;
    }

    public MachineParameter getHeatingPower() {
        return heatingPower;
    }
}
