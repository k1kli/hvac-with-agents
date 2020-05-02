package hvac.simulation.machinery;

public class AirConditioner {
    //TODO: calculate energy used
    private float coolingPower;//in watts
    private float exchangedAirVolumePerSecond;//in m^3/s
    private float waterRemoved;//as number from 0 to 1 (1 - all water removed)

    public float getCoolingPower() {
        return coolingPower;
    }

    public void setCoolingPower(float coolingPower) {
        this.coolingPower = coolingPower;
    }

    public float getExchangedAirVolumePerSecond() {
        return exchangedAirVolumePerSecond;
    }

    public void setExchangedAirVolumePerSecond(float exchangedAirVolumePerSecond) {
        this.exchangedAirVolumePerSecond = exchangedAirVolumePerSecond;
    }

    public float getWaterRemoved() {
        return waterRemoved;
    }

    public void setWaterRemoved(float waterRemoved) {
        this.waterRemoved = waterRemoved;
    }

    public AirConditioner(float coolingPower, float exchangedAirVolumePerSecond, float waterRemoved) {
        this.coolingPower = coolingPower;
        this.exchangedAirVolumePerSecond = exchangedAirVolumePerSecond;
        this.waterRemoved = waterRemoved;
    }
}
