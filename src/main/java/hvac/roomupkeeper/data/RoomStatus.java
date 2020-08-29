package hvac.roomupkeeper.data;

import java.time.LocalDateTime;

public class RoomStatus {
    private float temperatureSlope;
    private float heatingPower;
    private LocalDateTime time;

    public RoomStatus(float temperatureSlope, float heatingPower, LocalDateTime time) {
        this.temperatureSlope = temperatureSlope;
        this.heatingPower = heatingPower;
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
}
