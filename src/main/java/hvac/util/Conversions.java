package hvac.util;

import hvac.ontologies.machinery.MachineParameter;
import hvac.ontologies.weather.WeatherSnapshot;
import hvac.simulation.rooms.RoomClimate;

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

    public static hvac.ontologies.roomclimate.RoomClimate toOntologyRoomClimate(int roomId, RoomClimate roomClimate) {
        return new hvac.ontologies.roomclimate.RoomClimate(
                roomId,
                roomClimate.getTemperature(),
                roomClimate.getAbsoluteHumidity(),
                roomClimate.getRelativeHumidity(),
                roomClimate.getAirQuality());
    }

    public static hvac.ontologies.machinery.MachineParameter toOntologyParameter(hvac.simulation.machinery.MachineParameter parameter) {
        return new MachineParameter(parameter.getCurrentValue(), parameter.getMaxValue());
    }
}
