package hvac.util;

import hvac.ontologies.machinery.Machinery;
import hvac.ontologies.weather.WeatherSnapshot;
import hvac.simulation.rooms.Room;
import hvac.simulation.rooms.RoomMap;
import hvac.simulation.rooms.RoomWall;
import hvac.time.DateTimeSimulator;
import jade.core.Agent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class Helpers {
    public static void loadMap(RoomMap roomMap){
        Room r1 = new Room(1,2,200, 50, false);
        Room r2 = new Room(2,2,250, 70, true);
        Room r3 = new Room(3,3,150, 35, false);
        Room r4 = new Room(4,3,300, 80, true);
        RoomWall r12 = new RoomWall(24, 0.4f);
        RoomWall r23 = new RoomWall(16, 0.2f);
        RoomWall r34 = new RoomWall(18, 0.5f);
        RoomWall r41 = new RoomWall(40, 0.1f);
        roomMap.addRoom(r1);
        roomMap.addRoom(r2);
        roomMap.addRoom(r3);
        roomMap.addRoom(r4);
        roomMap.linkRooms(r1, r2, r12);
        roomMap.linkRooms(r2, r3, r23);
        roomMap.linkRooms(r3, r4, r34);
        roomMap.linkRooms(r4, r1, r41);
    }

    public static boolean isBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        return date.isAfter(start) && date.isBefore(end);
    }

    public static boolean initTimeFromArgs(Agent agent, Consumer<String> usageFunction) {
        Object[] arguments = agent.getArguments();
        if (arguments == null || arguments.length < 2) {
            usageFunction.accept("Wrong args num");
            agent.doDelete();
            return false;
        }
        LocalDateTime startTime;
        float timeScale;
        try {
            timeScale = Float.parseFloat(arguments[0].toString());
            startTime = LocalDateTime.parse(arguments[1].toString(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (NumberFormatException | DateTimeParseException e) {
            usageFunction.accept(e.getMessage());
            agent.doDelete();
            return false;
        }
        DateTimeSimulator.init(startTime, timeScale);
        return true;
    }
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean almostEqual(float f1, float f2, float epsilon) {
        return Math.abs(f1-f2)<epsilon;
    }
    public static void updateMachinery(Machinery updatedMachinery, Machinery updatingMachinery) {
        Optional.ofNullable(updatingMachinery.getAirConditioner()).ifPresent(airConditioner -> {
            Optional.ofNullable(airConditioner.getCoolingPower())
                    .ifPresent(coolingPower ->
                            updatedMachinery.getAirConditioner().getCoolingPower()
                                    .setCurrentValue(coolingPower.getCurrentValue()));
            Optional.ofNullable(airConditioner.getAirExchangedPerSecond())
                    .ifPresent(airExchangedPerSecond ->
                            updatedMachinery.getAirConditioner().getAirExchangedPerSecond()
                                    .setCurrentValue(airExchangedPerSecond.getCurrentValue()));
        });
        Optional.ofNullable(updatingMachinery.getHeater())
                .flatMap(heater -> Optional.ofNullable(heater.getHeatingPower()))
                .ifPresent(heatingPower ->
                        updatedMachinery.getHeater().getHeatingPower()
                                .setCurrentValue(heatingPower.getCurrentValue()));
        Optional.ofNullable(updatingMachinery.getVentilator())
                .flatMap(ventilator -> Optional.ofNullable(ventilator.getAirExchangedPerSecond()))
                .ifPresent(airExchangedPerSecond ->
                        updatedMachinery.getVentilator().getAirExchangedPerSecond()
                                .setCurrentValue(airExchangedPerSecond.getCurrentValue()));
    }

    public static WeatherSnapshot interpolateWeather(List<WeatherSnapshot> orderedWeatherSnapshots, LocalDateTime date) {
        switch (orderedWeatherSnapshots.size()) {
            case 0:
                throw new IllegalArgumentException("there hast to be at least one weather snapshot");
            case 1:
                return orderedWeatherSnapshots.get(0);
        }
        if(date.isBefore(Conversions.toLocalDateTime(orderedWeatherSnapshots.get(0).getTime()))) {
            return orderedWeatherSnapshots.get(0);
        }
        if(date.isAfter(Conversions.toLocalDateTime(orderedWeatherSnapshots.get(orderedWeatherSnapshots.size()-1).getTime()))
            || date.isEqual(Conversions.toLocalDateTime(orderedWeatherSnapshots.get(orderedWeatherSnapshots.size()-1).getTime()))) {
            return orderedWeatherSnapshots.get(orderedWeatherSnapshots.size()-1);
        }
        for(int i = 0; i< orderedWeatherSnapshots.size() - 1; i++) {
            if(date.isBefore(Conversions.toLocalDateTime(orderedWeatherSnapshots.get(i+1).getTime()))) {
                WeatherSnapshot first = orderedWeatherSnapshots.get(i);
                WeatherSnapshot second = orderedWeatherSnapshots.get(i+1);
                float ratio = (float)ChronoUnit.SECONDS.between(
                        Conversions.toLocalDateTime(first.getTime()),
                        date
                ) / ChronoUnit.SECONDS.between(
                        Conversions.toLocalDateTime(first.getTime()),
                        Conversions.toLocalDateTime(second.getTime())
                );
                return new WeatherSnapshot(Conversions.toDate(date),
                        first.getTemperature() * (1-ratio) + second.getTemperature() * ratio,
                        first.getPressure() * (1-ratio) + second.getPressure() * ratio,
                        first.getAbsoluteHumidity() * (1-ratio) + second.getAbsoluteHumidity() * ratio);
            }
        }
        return orderedWeatherSnapshots.get(0);//this should be unreachable
    }
}
