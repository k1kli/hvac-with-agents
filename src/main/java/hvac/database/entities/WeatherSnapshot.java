package hvac.database.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table( name = "WEATHER_SNAPSHOT",
    indexes = {
        @Index(columnList = "ID", name = "weather_id"),
        @Index(columnList = "WEATHER_TIMESTAMP", name="weather_timestamp")
    })
public class WeatherSnapshot {
    private Long id;
    private LocalDateTime date;
    private float temperatureKelvin;
    private float pressureHPa;
    private float absoluteHumidity;//in kg/m^3

    public WeatherSnapshot(){}//for hibernate

    public WeatherSnapshot(LocalDateTime date, float temperatureKelvin, float pressureHPa, float absoluteHumidity) {
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

    @Column(name = "WEATHER_TIMESTAMP")
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherSnapshot that = (WeatherSnapshot) o;
        return Float.compare(that.temperatureKelvin, temperatureKelvin) == 0 &&
                Float.compare(that.pressureHPa, pressureHPa) == 0 &&
                Float.compare(that.absoluteHumidity, absoluteHumidity) == 0 &&
                date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, temperatureKelvin, pressureHPa, absoluteHumidity);
    }
}
