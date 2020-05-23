package hvac.database.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table( name = "WEATHER_SNAPSHOT",
    indexes = {
        @Index(columnList = "ID", name = "weather_id"),
        @Index(columnList = "WEATHER_TIMESTAMP", name="weather_timestamp")
    })
public class WeatherSnapshot {
    private Long id;
    private Date date;
    private float temperatureKelvin;
    private float pressureHPa;
    private float absoluteHumidity;//in kg/m^3

    public WeatherSnapshot(){}//for hibernate

    public WeatherSnapshot(Date date, float temperatureKelvin, float pressureHPa, float absoluteHumidity) {
        //for application use
        this.date = date;
        this.temperatureKelvin = temperatureKelvin;
        this.pressureHPa = pressureHPa;
        this.absoluteHumidity = absoluteHumidity;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "WEATHER_TIMESTAMP")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Column(name = "TEMPERATURE")
    public float getTemperatureKelvin() {
        return temperatureKelvin;
    }

    public void setTemperatureKelvin(float temperatureKelvin) {
        this.temperatureKelvin = temperatureKelvin;
    }

    @Column(name = "PRESSURE")
    public float getPressureHPa() {
        return pressureHPa;
    }

    public void setPressureHPa(float pressureHPa) {
        this.pressureHPa = pressureHPa;
    }

    @Column(name = "HUMIDITY")
    public float getAbsoluteHumidity() {
        return absoluteHumidity;
    }

    public void setAbsoluteHumidity(float absoluteHumidity) {
        this.absoluteHumidity = absoluteHumidity;
    }
}
