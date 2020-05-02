package hvac.simulation.machinery;

public class Ventilator {
    //TODO: calculate energy used
    private float exchangedAirVolumePerSecond;//in m^3/s

    public Ventilator(float exchangedAirVolumePerSecond) {
        this.exchangedAirVolumePerSecond = exchangedAirVolumePerSecond;
    }

    public float getExchangedAirVolumePerSecond() {
        return exchangedAirVolumePerSecond;
    }

    public void setExchangedAirVolumePerSecond(float exchangedAirVolumePerSecond) {
        this.exchangedAirVolumePerSecond = exchangedAirVolumePerSecond;
    }
}
