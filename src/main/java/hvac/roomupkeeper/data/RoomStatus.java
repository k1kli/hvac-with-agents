package hvac.roomupkeeper.data;

import java.time.LocalDateTime;

public class RoomStatus {
    private float temperatureSlope;
    private float humiditySlope;
    /**
     * positive -> heater.power=value, ac.power = 0
     * negative -> heater.power = 0, ac.power = -value
     */
    private float heatingPower;
    /**
     * positive -> ac.airPerSecond = 0, ventilation.airPerSecond = value + minimalRequiredVentilation
     * negative -> ac.airPerSecond = -value, ventilation.airPerSecond = minimalRequiredVentilation
     * so its the same as with heating power -> ac counteracts ventilation - its just that the tipping point is not at zero
     * but at minimalRequiredVentilation
     */
    private float airExchangedPerSecond;
    private LocalDateTime time;

    public RoomStatus(float temperatureSlope, float humiditySlope, float heatingPower, float airExchangedPerSecond, LocalDateTime time) {
        this.temperatureSlope = temperatureSlope;
        this.humiditySlope = humiditySlope;
        this.heatingPower = heatingPower;
        this.airExchangedPerSecond = airExchangedPerSecond;
        this.time = time;
    }

    public float getTemperatureSlope() {
        return temperatureSlope;
    }

    public void setTemperatureSlope(float temperatureSlope) {
        this.temperatureSlope = temperatureSlope;
    }

    public float getHeatingPower() {
        return heatingPower;
    }

    public void setHeatingPower(float heatingPower) {
        this.heatingPower = heatingPower;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public float getHumiditySlope() {
        return humiditySlope;
    }

    public void setHumiditySlope(float humiditySlope) {
        this.humiditySlope = humiditySlope;
    }

    public float getAirExchangedPerSecond() {
        return airExchangedPerSecond;
    }

    public void setAirExchangedPerSecond(float airExchangedPerSecond) {
        this.airExchangedPerSecond = airExchangedPerSecond;
    }
}
