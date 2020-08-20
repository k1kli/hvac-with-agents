package hvac.simulation.rooms;

import hvac.simulation.machinery.AirConditioner;
import hvac.simulation.machinery.Heater;
import hvac.simulation.machinery.Ventilator;

public class RoomClimate {
    private float temperature;//in kelvins
    private final Heater heater;
    private final AirConditioner airConditioner;
    private final Ventilator ventilator;
    private float absoluteHumidity;//in kg/m^3
    private float relativeHumidity;//ratio (no unit)
    private int peopleInRoom = 0;
    private float airQuality;//ratio (no unit)

    public RoomClimate(Heater heater, AirConditioner airConditioner, Ventilator ventilator) {
        this.heater = heater;
        this.airConditioner = airConditioner;
        this.ventilator = ventilator;
    }

    public float getTemperature() {
        return temperature;
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

    public void setAbsoluteHumidity(float absoluteHumidity) {
        this.absoluteHumidity = absoluteHumidity;
    }

    public void setRelativeHumidity(float relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public void setPeopleInRoom(int peopleInRoom) {
        this.peopleInRoom = peopleInRoom;
    }

    public Heater getHeater() {
        return heater;
    }

    public AirConditioner getAirConditioner() {
        return airConditioner;
    }

    public Ventilator getVentilator() {
        return ventilator;
    }

    public int getPeopleInRoom() {
        return peopleInRoom;
    }

    public float getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(float airQuality) {
        this.airQuality = airQuality;
    }
}
