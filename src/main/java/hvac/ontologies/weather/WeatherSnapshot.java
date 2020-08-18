package hvac.ontologies.weather;

import jade.content.Concept;

import java.util.Date;

public class WeatherSnapshot implements Concept {
    private Date time;
    private float temperature;
    private float pressure;
    private float absoluteHumidity;

    public WeatherSnapshot() {}
    public WeatherSnapshot(Date time, float temperature, float pressure, float absoluteHumidity) {
        this.time = time;
        this.temperature = temperature;
        this.pressure = pressure;
        this.absoluteHumidity = absoluteHumidity;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getAbsoluteHumidity() {
        return absoluteHumidity;
    }

    public void setAbsoluteHumidity(float absoluteHumidity) {
        this.absoluteHumidity = absoluteHumidity;
    }
}
