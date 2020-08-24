package hvac.ontologies.roomclimate;

import jade.content.Predicate;

public class RoomClimate implements Predicate {
    private int roomId;
    private float temperature;//in kelvins
    private float absoluteHumidity;//in kg/m^3
    private float relativeHumidity;//ratio (no unit)
    private float airQuality;//ratio (no unit)

    public RoomClimate() {}

    public RoomClimate(int roomId, float temperature, float absoluteHumidity, float relativeHumidity, float airQuality) {
        this.roomId = roomId;
        this.temperature = temperature;
        this.absoluteHumidity = absoluteHumidity;
        this.relativeHumidity = relativeHumidity;
        this.airQuality = airQuality;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getAbsoluteHumidity() {
        return absoluteHumidity;
    }

    public void setAbsoluteHumidity(float absoluteHumidity) {
        this.absoluteHumidity = absoluteHumidity;
    }

    public float getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(float relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public float getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(float airQuality) {
        this.airQuality = airQuality;
    }
}
