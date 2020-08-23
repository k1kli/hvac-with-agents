package hvac.simulation.machinery;

public class Ventilator {
    //TODO: calculate energy used
    private final MachineParameter exchangedAirVolumePerSecond;//in m^3/s

    public Ventilator(MachineParameter exchangedAirVolumePerSecond) {
        this.exchangedAirVolumePerSecond = exchangedAirVolumePerSecond;
    }

    public MachineParameter getExchangedAirVolumePerSecond() {
        return exchangedAirVolumePerSecond;
    }
}
