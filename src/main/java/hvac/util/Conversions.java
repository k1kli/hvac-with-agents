package hvac.util;

import hvac.ontologies.machinery.MachineParameter;
import hvac.ontologies.weather.WeatherSnapshot;
import hvac.simulation.rooms.RoomClimate;
import jade.util.leap.ArrayList;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @SuppressWarnings("unchecked")
    public static <T> List<T> toJavaList(jade.util.leap.List jadeList) {
        try {
            return Arrays.stream(jadeList.toArray()).map(it->(T)it).collect(Collectors.toList());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("elements of given jade list are not of type required for result list");
        }
    }

    public static <T> jade.util.leap.List toJadeList(List<T> javaList) {
        ArrayList arrayList = new ArrayList();
        javaList.forEach(arrayList::add);
        return arrayList;
    }
}
