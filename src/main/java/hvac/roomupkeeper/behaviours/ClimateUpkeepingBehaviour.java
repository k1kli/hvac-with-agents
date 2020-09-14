package hvac.roomupkeeper.behaviours;

import hvac.ontologies.machinery.*;
import hvac.ontologies.roomclimate.RoomClimate;
import hvac.ontologies.weather.Forecast;
import hvac.ontologies.weather.WeatherSnapshot;
import hvac.roomupkeeper.RoomUpkeeperContext;
import hvac.roomupkeeper.data.RoomStatus;
import hvac.simulation.SimulationAgentMessenger;
import hvac.time.DateTimeSimulator;
import hvac.util.Conversions;
import hvac.util.Helpers;
import hvac.util.Interpolation;
import hvac.weatherforecaster.WeatherForecasterMessenger;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ClimateUpkeepingBehaviour extends CyclicBehaviour {
    private final RoomUpkeeperContext context;
    private Step step = Step.INIT;
    private MessageTemplate currentTemplate;
    private static final int CLIMATE_FORGET_TIME_SECONDS = 3600 * 2;
    private static final int MINUTES_BETWEEN_UPDATES = 3;
    private static final float POWER_TOLERANCE = 0.5f;
    private static final float AIR_EXCHANGED_PER_SECOND_TOLERANCE = 0.1f;
    private static final float SLOPE_TOLERANCE = 0.000001f;
    private LocalDateTime lastUpdate = LocalDateTime.MIN;

    private enum Step {
        INIT,
        CLIMATE_WAIT_FOR_RESPONSE,
        MACHINERY_INFO_WAIT_FOR_RESPONSE,
        MACHINERY_UPDATE_WAIT_FOR_RESPONSE,
        WEATHER_FORECASTER_WAIT_FOR_RESPONSE
    }

    private RoomClimate currentClimate = null;
    private final List<RoomStatus> roomStatuses = new ArrayList<>();

    private Machinery currentMachinery = null;
    private List<WeatherSnapshot> weatherSnapshots = new ArrayList<>();

    public ClimateUpkeepingBehaviour(Agent a, RoomUpkeeperContext context) {
        super(a);
        this.context = context;
    }

    @Override
    public void action() {
        //remove expired statuses, so only recent data is used in interpolation
        roomStatuses.removeIf(status -> status.getTime()
                .isBefore(DateTimeSimulator.getCurrentDate().minusSeconds(CLIMATE_FORGET_TIME_SECONDS)));
        context.removeCompleteMeetings();
        switch (step) {
            case INIT:
                initStep();
                break;
            case MACHINERY_INFO_WAIT_FOR_RESPONSE:
                machineryInfoWaitForResponseStep();
                break;
            case WEATHER_FORECASTER_WAIT_FOR_RESPONSE:
                weatherForecasterWaitForResponseStep();
                break;
            case CLIMATE_WAIT_FOR_RESPONSE:
                climateWaitForResponseStep();
                break;
            case MACHINERY_UPDATE_WAIT_FOR_RESPONSE:
                machineryUpdateWaitForResponseStep();
        }
    }

    private void initStep() {
        if (context.getNextMeeting() == null) {
            if(isCurrentMachineryZeroed()) {
                block(1000);
                return;
            } else {
                zeroCurrentMachinery();
            }
        }
        decideNextStep();
    }

    private boolean isCurrentMachineryZeroed() {
        return currentMachinery == null || Stream.of(
                currentMachinery.getAirConditioner().getAirExchangedPerSecond(),
                currentMachinery.getVentilator().getAirExchangedPerSecond(),
                currentMachinery.getAirConditioner().getCoolingPower(),
                currentMachinery.getHeater().getHeatingPower()
        ).allMatch(parameter -> Helpers.almostEqual(parameter.getCurrentValue(), 0.0f, 0.00001f));
    }

    private void zeroCurrentMachinery() {
        MachineParameter zeroParameter = new MachineParameter(0.0f, null);
        Machinery machinery = new Machinery(
                new AirConditioner(
                        zeroParameter,
                        zeroParameter
                ),
                new Heater(zeroParameter),
                new Ventilator(zeroParameter)
        );
        Helpers.updateMachinery(currentMachinery, machinery);
        enterMachineryUpdateWaitForResponseStep(machinery);
    }


    private void machineryInfoWaitForResponseStep() {
        ACLMessage msg = myAgent.receive(currentTemplate);
        if (msg != null) {
            try {
                Optional<MachineryStatus> machineryStatusOpt = SimulationAgentMessenger.extractMachineryStatus(myAgent, msg);
                machineryStatusOpt.ifPresent(status -> currentMachinery = status.getMachinery());
                if (!machineryStatusOpt.isPresent()) {
                    throw new RuntimeException("simulation agent returns incorrect messages");
                }
            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
            }
            decideNextStep();
        } else {
            block();
        }
    }

    private void weatherForecasterWaitForResponseStep() {
        ACLMessage msg = myAgent.receive(currentTemplate);
        if (msg != null) {
            try {
                Optional<Forecast> forecastOpt = WeatherForecasterMessenger.extractForecast(myAgent, msg);
                forecastOpt.ifPresent(forecast -> weatherSnapshots = Conversions.toJavaList(forecast.getWeatherSnapshots()));
                if (!forecastOpt.isPresent()) {
                    throw new RuntimeException("weather forecaster agent returns incorrect messages");
                }
            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
            }
            decideNextStep();
        } else {
            block();
        }
    }

    private void climateWaitForResponseStep() {
        ACLMessage msg = myAgent.receive(currentTemplate);
        if (msg != null) {
            try {
                Optional<RoomClimate> roomClimateOpt = SimulationAgentMessenger.extractRoomClimate(myAgent, msg);
                roomClimateOpt.ifPresent(climate -> {
                    LocalDateTime currentDate = DateTimeSimulator.getCurrentDate();
                    if (currentClimate != null) {
                        long secondsSinceLastUpdate = ChronoUnit.SECONDS.between(
                                lastUpdate,
                                currentDate
                        );
                        float temperatureSlope = (climate.getTemperature() - currentClimate.getTemperature()) /
                                secondsSinceLastUpdate;
                        float humiditySlope = (climate.getRelativeHumidity() - currentClimate.getRelativeHumidity()) /
                                secondsSinceLastUpdate;
                        float heatingPower = currentMachinery.getHeater().getHeatingPower().getCurrentValue() -
                                currentMachinery.getAirConditioner().getCoolingPower().getCurrentValue();
                        float airExchangedPerSecond =
                                currentMachinery.getVentilator().getAirExchangedPerSecond().getCurrentValue()
                                        -currentMachinery.getAirConditioner().getAirExchangedPerSecond().getCurrentValue()
                                                - context.getRequiredVentilation();
                        roomStatuses.add(new RoomStatus(
                                temperatureSlope,
                                humiditySlope, heatingPower,
                                airExchangedPerSecond, currentDate));
                    }
                    currentClimate = climate;
                });
                if (!roomClimateOpt.isPresent()) {
                    throw new RuntimeException("Simulation agent returns incorrect messages");
                }
                if (roomStatuses.size() == 0) {
                    //get some more data required to kickstart the interpolation process
                    lastUpdate = DateTimeSimulator.getCurrentDate();
                    decideNextStep();
                    return;
                } else if (roomStatuses.size() == 1) {
                    //kickstart the interpolation process
                    //first and last points need to
                    roomStatuses.add(new RoomStatus(
                            100.0f,
                            100.0f,
                            10000000.0f,
                            10000000.0f,
                            LocalDateTime.MAX));
                    roomStatuses.add(new RoomStatus(
                            -100.0f,
                            -100.0f,
                            -10000000.0f,
                            -10000000.0f,
                            LocalDateTime.MAX));
                }
                updateMachinery();
            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private void machineryUpdateWaitForResponseStep() {
        ACLMessage msg = myAgent.receive(currentTemplate);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.AGREE) {
                step = Step.INIT;
                lastUpdate = DateTimeSimulator.getCurrentDate();
            }
            decideNextStep();
        } else {
            block();
        }
    }

    private void decideNextStep() {
        if (context.getNextMeeting() == null) {
            step = Step.INIT;
            return;
        }
        if (currentMachinery == null) {
            enterMachineryInfoWaitForResponseStep();
        } else if (weatherSnapshots.stream().noneMatch(snapshot ->
                Conversions.toLocalDateTime(snapshot.getTime())
                        .isAfter(DateTimeSimulator.getCurrentDate().plusDays(2)))) {
            enterWeatherForecasterWaitForResponseStep();
        } else if (DateTimeSimulator.getCurrentDate().isBefore(lastUpdate.plusMinutes(MINUTES_BETWEEN_UPDATES))) {
            long blockTime = (long) (ChronoUnit.MILLIS.between(DateTimeSimulator.getCurrentDate(),
                    lastUpdate.plusMinutes(MINUTES_BETWEEN_UPDATES)) / DateTimeSimulator.getTimeScale());
            step = Step.INIT;
            if (blockTime > 0)
                block(blockTime);
        } else {
            enterClimateWaitForResponseStep();
        }
    }

    private void updateMachinery() {
        Machinery machinery = prepareNextRequiredMachinery();
        if (!(machinery.getHeater() == null
                && machinery.getAirConditioner() == null
                && machinery.getVentilator() == null)) {
            Helpers.updateMachinery(currentMachinery, machinery);
            enterMachineryUpdateWaitForResponseStep(machinery);
        } else {
            lastUpdate = DateTimeSimulator.getCurrentDate();
            decideNextStep();
        }
    }

    private Machinery prepareNextRequiredMachinery() {
        float requiredTemperatureSlope = getRequiredTemperatureSlope();
        float temperatureMaintainingHeatingPower = calculateTemperatureMaintainingHeatingPower(requiredTemperatureSlope);
        float requiredHumiditySlope = getRequiredHumiditySlope();
        float humidityMaintainingAirPerSecond = calculateHumidityMaintainingAirPerSecond(requiredHumiditySlope);
        return new Machinery(
                prepareNextAirConditioner(temperatureMaintainingHeatingPower, humidityMaintainingAirPerSecond),
                prepareNextHeater(temperatureMaintainingHeatingPower),
                prepareNextVentilator(humidityMaintainingAirPerSecond)
        );
    }

    private float getRequiredTemperatureSlope() {

        if (meetingInProgress()) {
            return (context.getNextMeeting().getTemperature()
                    - currentClimate.getTemperature()) / ChronoUnit.SECONDS.between(
                    DateTimeSimulator.getCurrentDate(),
                    DateTimeSimulator.getCurrentDate().plusMinutes(MINUTES_BETWEEN_UPDATES));
        } else {
            return (context.getNextMeeting().getTemperature()
                    - currentClimate.getTemperature()) / ChronoUnit.SECONDS.between(
                    DateTimeSimulator.getCurrentDate(),
                    context.getNextMeeting().getLocalStartDate());
        }
    }

    private float getRequiredHumiditySlope() {

        if (meetingInProgress()) {
            return (context.getRelativeHumidityToMaintain()
                    - currentClimate.getRelativeHumidity()) / ChronoUnit.SECONDS.between(
                    DateTimeSimulator.getCurrentDate(),
                    DateTimeSimulator.getCurrentDate().plusMinutes(MINUTES_BETWEEN_UPDATES));
        } else {
            return (context.getRelativeHumidityToMaintain()
                    - currentClimate.getRelativeHumidity()) / ChronoUnit.SECONDS.between(
                    DateTimeSimulator.getCurrentDate(),
                    context.getNextMeeting().getLocalStartDate());
        }
    }

    private float calculateTemperatureMaintainingHeatingPower(float requiredTemperatureSlope) {
        //not include older statuses that have the same slope as the newer ones
        //as these slopes will be used as interpolation knots arguments and have to be distinct
        Stream<RoomStatus> statusesWithUniqueTempSlopes = roomStatuses.stream()
                .filter(status ->
                        roomStatuses
                                .stream()
                                .noneMatch(otherStatus ->
                                        Helpers.almostEqual(
                                                status.getTemperatureSlope(),
                                                otherStatus.getTemperatureSlope(),
                                                SLOPE_TOLERANCE)
                                                && otherStatus.getTime().isAfter(status.getTime())));
        List<Float> arguments = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        statusesWithUniqueTempSlopes
                .sorted(Comparator.comparing(RoomStatus::getTemperatureSlope))
                .forEachOrdered(status -> {
                    arguments.add(status.getTemperatureSlope());
                    values.add(status.getHeatingPower());
                });
        float unboundRequiredHeatingPower = Interpolation.calculateValueAt(requiredTemperatureSlope,
                arguments, values);
        return Math.max(Math.min(unboundRequiredHeatingPower,
                currentMachinery.getHeater().getHeatingPower().getMaxValue()),
                -currentMachinery.getAirConditioner().getCoolingPower().getMaxValue());
    }

    private float calculateHumidityMaintainingAirPerSecond(float requiredHumiditySlope) {
        //not include older statuses that have the same slope as the newer ones
        //as these slopes will be used as interpolation knots arguments and have to be distinct
        Stream<RoomStatus> statusesWithUniqueHumiditySlopes = roomStatuses.stream()
                .filter(status ->
                        roomStatuses
                                .stream()
                                .noneMatch(otherStatus ->
                                        Helpers.almostEqual(
                                                status.getHumiditySlope(),
                                                otherStatus.getHumiditySlope(),
                                                SLOPE_TOLERANCE)
                                                && otherStatus.getTime().isAfter(status.getTime())));
        List<Float> arguments = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        statusesWithUniqueHumiditySlopes
                .sorted(Comparator.comparing(RoomStatus::getHumiditySlope))
                .forEachOrdered(status -> {
                    arguments.add(status.getHumiditySlope());
                    values.add(status.getAirExchangedPerSecond());
                });
        float unboundRequiredAirPerSecond = Interpolation.calculateValueAt(requiredHumiditySlope,
                arguments, values);

        float minAirPerSecond
                = -currentMachinery.getAirConditioner().getAirExchangedPerSecond().getMaxValue();
        float maxAirPerSecond = Math.max(0, currentMachinery.getVentilator().getAirExchangedPerSecond().getMaxValue()
                - context.getRequiredVentilation());
        return Math.max(Math.min(unboundRequiredAirPerSecond,
                maxAirPerSecond),
                minAirPerSecond);
    }

    private AirConditioner prepareNextAirConditioner(float temperatureMaintainingHeatingPower, float humidityMaintainingAirPerSecond) {
        MachineParameter nextACAirPerSecond = prepareNextACAirPerSecond(humidityMaintainingAirPerSecond);
        MachineParameter nextACCoolingPower = prepareNextACCoolingPower(temperatureMaintainingHeatingPower);
        if (nextACAirPerSecond == null && nextACCoolingPower == null) {
            return null;
        } else {
            return new AirConditioner(nextACAirPerSecond, nextACCoolingPower);
        }
    }

    private MachineParameter prepareNextACAirPerSecond(float humidityMaintainingAirPerSecond) {
        float actualAirConditioningAirPerSecondRequired
                = -humidityMaintainingAirPerSecond;
        if (actualAirConditioningAirPerSecondRequired > AIR_EXCHANGED_PER_SECOND_TOLERANCE) {
            return new MachineParameter(
                    Math.min(
                            currentMachinery.getAirConditioner().getAirExchangedPerSecond().getMaxValue(),
                            actualAirConditioningAirPerSecondRequired),
                    null);
        } else if (!Helpers.almostEqual(
                0.0f,
                currentMachinery.getAirConditioner().getAirExchangedPerSecond().getCurrentValue(),
                AIR_EXCHANGED_PER_SECOND_TOLERANCE)) {
            return new MachineParameter(0.0f, null);
        }
        return null;
    }

    private MachineParameter prepareNextACCoolingPower(float temperatureMaintainingHeatingPower) {
        float actualCoolingPowerRequired = -temperatureMaintainingHeatingPower;
        if (actualCoolingPowerRequired > POWER_TOLERANCE) {
            return new MachineParameter(actualCoolingPowerRequired, null);
        } else if (!Helpers.almostEqual(
                0.0f,
                currentMachinery.getAirConditioner().getCoolingPower().getCurrentValue(),
                POWER_TOLERANCE)) {
            return new MachineParameter(0.0f, null);
        }
        return null;
    }

    private Ventilator prepareNextVentilator(float humidityMaintainingAirPerSecond) {
        float actualVentilationRequired = Math.min(
                Math.max(humidityMaintainingAirPerSecond, 0)
                        + context.getRequiredVentilation(),
                currentMachinery.getVentilator().getAirExchangedPerSecond().getMaxValue());
        if (!Helpers.almostEqual(
                actualVentilationRequired,
                currentMachinery.getVentilator().getAirExchangedPerSecond().getCurrentValue(),
                AIR_EXCHANGED_PER_SECOND_TOLERANCE)) {
            return new Ventilator(
                    new MachineParameter(
                            actualVentilationRequired,
                            null)
            );
        }
        return null;
    }

    private Heater prepareNextHeater(float temperatureMaintainingHeatingPower) {
        if (temperatureMaintainingHeatingPower > POWER_TOLERANCE) {
            return new Heater(new MachineParameter(
                    temperatureMaintainingHeatingPower, null
            ));
        } else if (!Helpers.almostEqual(
                0.0f,
                currentMachinery.getHeater().getHeatingPower().getCurrentValue(),
                POWER_TOLERANCE)) {
            return new Heater(new MachineParameter(
                    0.0f, null
            ));
        }
        return null;
    }

    private boolean meetingInProgress() {
        return context.getNextMeeting().getLocalStartDate().isBefore(DateTimeSimulator.getCurrentDate())
                &&
                context.getNextMeeting().getLocalEndDate().isAfter(DateTimeSimulator.getCurrentDate());
    }

    private void enterMachineryInfoWaitForResponseStep() {
        context.getLogger().log("entering MachineryInfoWaitForResponseStep");
        try {
            ACLMessage msg = SimulationAgentMessenger.prepareReportMachineryStatus(
                    context.getMyRoomId(), myAgent, context.getSimulationAgent());
            myAgent.send(msg);
            currentTemplate = MessageTemplate.MatchSender(context.getSimulationAgent());
            step = Step.MACHINERY_INFO_WAIT_FOR_RESPONSE;
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
    }

    private void enterWeatherForecasterWaitForResponseStep() {
        context.getLogger().log("entering WeatherForecasterWaitForResponseStep");
        try {
            ACLMessage msg = WeatherForecasterMessenger.prepareForecastRequest(
                    DateTimeSimulator.getCurrentDate(),
                    DateTimeSimulator.getCurrentDate().plusWeeks(1),
                    myAgent,
                    context.getWeatherForecaster());
            myAgent.send(msg);
            currentTemplate = MessageTemplate.MatchSender(context.getWeatherForecaster());
            step = Step.WEATHER_FORECASTER_WAIT_FOR_RESPONSE;
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
    }

    private void enterClimateWaitForResponseStep() {
        context.getLogger().log("entering ClimateWaitForResponseStep");
        try {
            ACLMessage msg = SimulationAgentMessenger.prepareInfoRequest(
                    context.getMyRoomId(),
                    myAgent,
                    context.getSimulationAgent());
            myAgent.send(msg);
            currentTemplate = MessageTemplate.MatchSender(context.getSimulationAgent());
            step = Step.CLIMATE_WAIT_FOR_RESPONSE;
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
    }

    private void enterMachineryUpdateWaitForResponseStep(Machinery update) {
        context.getLogger().log("entering MachineryUpdateWaitForResponseStep");
        try {
            ACLMessage msg = SimulationAgentMessenger.prepareUpdateMachinery(
                    update,
                    context.getMyRoomId(),
                    myAgent,
                    context.getSimulationAgent());
            myAgent.send(msg);
            currentTemplate = MessageTemplate.MatchSender(context.getSimulationAgent());
            step = Step.MACHINERY_UPDATE_WAIT_FOR_RESPONSE;
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
    }
}
