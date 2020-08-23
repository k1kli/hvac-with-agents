package hvac.simulation.machinery;

public class AirConditioner {
    //TODO: calculate energy used
    private final MachineParameter coolingPower;//in watts
    private final MachineParameter exchangedAirVolumePerSecond;//in m^3/s
    private final float waterRemoved;//as number from 0 to 1 (1 - all water removed)

    public AirConditioner(MachineParameter coolingPower,
                          MachineParameter exchangedAirVolumePerSecond,
                          float waterRemoved) {
        this.coolingPower = coolingPower;
        this.exchangedAirVolumePerSecond = exchangedAirVolumePerSecond;
        this.waterRemoved = waterRemoved;
    }

    public MachineParameter getCoolingPower() {
        return coolingPower;
    }

    public MachineParameter getExchangedAirVolumePerSecond() {
        return exchangedAirVolumePerSecond;
    }

    public float getWaterRemoved() {
        return waterRemoved;
    }

}
