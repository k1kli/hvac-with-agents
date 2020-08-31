package hvac.roomupkeeper.data;

import java.time.LocalDateTime;

public class RoomStatus {
    private float temperatureSlope;
    private float humiditySlope;
    /**
     * positive - power of the heater
     * negative - power of the air conditioner
     */
    private float heatingPower;
    /**
     * positive - air exchanged per second by ventilation - minimal required ventilation
     * negative - air exchanged per second by air conditioning - minimal required ventilation
     * so it is zero when ventilation works on minimal required aeps
     * and ac works enough to counteract humidification created by ventilation
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
