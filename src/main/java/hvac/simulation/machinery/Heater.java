package hvac.simulation.machinery;

public class Heater {
    //TODO: calculate energy used
    private float heatingPower;// in watts

    public float getHeatingPower() {
        return heatingPower;
    }

    public void setHeatingPower(float heatingPower) {
        this.heatingPower = heatingPower;
    }


    public Heater(float heatingPower) {
        this.heatingPower = heatingPower;
    }
}
