package hvac.weather.interfaces;

import hvac.database.entities.WeatherSnapshot;

import java.time.LocalDateTime;

public interface ForecastProvider {
    WeatherSnapshot[] getWeatherBetween(LocalDateTime d1, LocalDateTime d2);
    WeatherSnapshot getWeatherAt(LocalDateTime d);
}
