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
        Room r1 = new Room(1,2,200, 50, true);
        Room r2 = new Room(2,2,250, 70, true);
        Room r3 = new Room(3,3,150, 35, true);
        Room r4 = new Room(4,3,300, 80, true);
        Room r5 = new Room(5,1,100, 25, true);
        Room r6 = new Room(6,2,200, 50, true);
        Room r7 = new Room(7,3,300, 80, true);
        Room r8 = new Room(8,4,500, 100, true);
        RoomWall r12 = new RoomWall(24, 0.4f);
        RoomWall r23 = new RoomWall(16, 0.2f);
        RoomWall r34 = new RoomWall(18, 0.5f);
        RoomWall r41 = new RoomWall(40, 0.1f);
        RoomWall r45 = new RoomWall(22, 0.1f);
        RoomWall r46 = new RoomWall(34, 0.2f);
        RoomWall r56 = new RoomWall(24, 0.2f);
        RoomWall r26 = new RoomWall(26, 0.3f);
        RoomWall r17 = new RoomWall(30, 0.4f);
        RoomWall r47 = new RoomWall(11, 0.1f);
        RoomWall r38 = new RoomWall(12, 0.3f);
        RoomWall r58 = new RoomWall(15, 0.2f);
        RoomWall r78 = new RoomWall(22, 0.1f);
        roomMap.addRoom(r1);
        roomMap.addRoom(r2);
        roomMap.addRoom(r3);
        roomMap.addRoom(r4);
        roomMap.addRoom(r5);
        roomMap.addRoom(r6);
        roomMap.addRoom(r7);
        roomMap.addRoom(r8);
        roomMap.linkRooms(r1, r2, r12);
        roomMap.linkRooms(r2, r3, r23);
        roomMap.linkRooms(r3, r4, r34);
        roomMap.linkRooms(r4, r1, r41);
        roomMap.linkRooms(r4, r5, r45);
        roomMap.linkRooms(r4, r6, r46);
        roomMap.linkRooms(r5, r6, r56);
        roomMap.linkRooms(r2, r6, r26);
        roomMap.linkRooms(r1, r7, r17);
        roomMap.linkRooms(r4, r7, r47);
        roomMap.linkRooms(r3, r8, r38);
        roomMap.linkRooms(r5, r8, r58);
        roomMap.linkRooms(r7, r8, r78);
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
