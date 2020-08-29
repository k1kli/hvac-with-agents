package hvac.util;

import hvac.ontologies.machinery.Machinery;
import hvac.time.DateTimeSimulator;
import jade.core.Agent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Consumer;

public class Helpers {
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
}
