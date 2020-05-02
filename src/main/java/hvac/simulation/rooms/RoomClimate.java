package hvac.simulation.rooms;

public class RoomClimate {
    public float getTemperature() {
        return temperature;
    }

    public float getHeaterPower() {
        return heaterPower;
    }

    public float getAcPower() {
        return acPower;
    }

    public float getVentilation() {
        return ventilation;
    }

    public float getAbsoluteHumidity() {
        return absoluteHumidity;
    }

    public float getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public void setHeaterPower(float heaterPower) {
        this.heaterPower = heaterPower;
    }

    public void setAcPower(float acPower) {
        this.acPower = acPower;
    }

    public void setVentilation(float ventilation) {
        this.ventilation = ventilation;
    }

    public void setAbsoluteHumidity(float absoluteHumidity) {
        this.absoluteHumidity = absoluteHumidity;
    }

    public void setRelativeHumidity(float relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public void setPeopleInRoom(int peopleInRoom) {
        this.peopleInRoom = peopleInRoom;
    }

    public int getPeopleInRoom() {
        return peopleInRoom;
    }

    private float temperature;//in kelvins
    private float heaterPower;//in watts
    private float acPower;//in watts
    private float ventilation;// in m3 per second
    private float absoluteHumidity;//in kg/m^3
    private float relativeHumidity;//ratio (no unit)
    private int peopleInRoom = 0;
}
