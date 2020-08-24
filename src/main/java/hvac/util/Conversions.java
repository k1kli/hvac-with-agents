package hvac.util;

import hvac.ontologies.weather.WeatherSnapshot;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Conversions {
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
    public static Date toDate(LocalDateTime date) {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static hvac.ontologies.weather.WeatherSnapshot toOntologySnapshot(hvac.database.entities.WeatherSnapshot weatherSnapshot) {
        return new WeatherSnapshot(
                toDate(weatherSnapshot.getDate()),
                weatherSnapshot.getTemperature(),
                weatherSnapshot.getPressure(),
                weatherSnapshot.getAbsoluteHumidity());
    }
}
