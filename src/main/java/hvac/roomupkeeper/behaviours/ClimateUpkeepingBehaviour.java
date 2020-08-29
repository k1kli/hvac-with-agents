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

public class ClimateUpkeepingBehaviour extends CyclicBehaviour {
    private final RoomUpkeeperContext context;
    private Step step = Step.INIT;
    private MessageTemplate currentTemplate;
    private static final int CLIMATE_FORGET_TIME_SECONDS = 3600 * 2;
    private static final int MINUTES_BETWEEN_UPDATES = 3;
    private static final float AIR_QUALITY_TOLERANCE = 0.05f;
    private static final float POWER_TOLERANCE = 0.5f;
    private static final float AIR_EXCHANGED_PER_SECOND_TOLERANCE = 0.1f;
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
    private boolean started = false;

    public ClimateUpkeepingBehaviour(Agent a, RoomUpkeeperContext context) {
        super(a);
        this.context = context;
    }

    @Override
    public void action() {
        //wait for other agents to initialize
        if(!started) {
            started = true;
            block(1000);
            return;
        }
        //remove expired statuses, so only recent data is used in interpolation
        roomStatuses.removeIf(status->status.getTime()
                .isBefore(DateTimeSimulator.getCurrentDate().minusSeconds(CLIMATE_FORGET_TIME_SECONDS)));
        //remove statuses that have the same temperature slope as newer ones
        //temperature slopes are x's in the interpolated function so they can't appear twice
        roomStatuses.removeIf(status->roomStatuses.stream()
                .anyMatch(status2->
                        Helpers.almostEqual(
                                status2.getTemperatureSlope(),
                                status.getTemperatureSlope(),
                                0.00001f) &&
                        status2.getTime().isAfter(status.getTime())));
        //current meeting is over - delete it
        if(context.getNextMeeting() != null && Conversions.toLocalDateTime(context.getNextMeeting().getEndDate())
                .isBefore(DateTimeSimulator.getCurrentDate())) {
            context.setNextMeeting(null);
        }
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
            block();
            return;
        }
        decideNextStep();
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
                        roomStatuses.add(new RoomStatus(
                                (climate.getTemperature() - currentClimate.getTemperature()) /
                                        ChronoUnit.SECONDS.between(
                                                lastUpdate,
                                                currentDate),
                                currentMachinery.getHeater().getHeatingPower().getCurrentValue() -
                                        currentMachinery.getAirConditioner().getCoolingPower().getCurrentValue(),
                                currentDate));
                    }
                    currentClimate = climate;
                });
                if (!roomClimateOpt.isPresent()) {
                    throw new RuntimeException("Simulation agent returns incorrect messages");
                }
                if(roomStatuses.size() == 0) {
                    //get some more data required to kickstart the interpolation process
                    lastUpdate = DateTimeSimulator.getCurrentDate();
                    decideNextStep();
                    return;
                } else if(roomStatuses.size() == 1) {
                    //kickstart the interpolation process
                    //first and last points need to
                        roomStatuses.add(new RoomStatus(
                                100.0f,
                                10000000.0f,
                                LocalDateTime.MAX));
                        roomStatuses.add(new RoomStatus(
                                -100.0f,
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
            if(msg.getPerformative() == ACLMessage.AGREE) {
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
        } else if(DateTimeSimulator.getCurrentDate().isBefore(lastUpdate.plusMinutes(MINUTES_BETWEEN_UPDATES))) {
            long blockTime = (long) (ChronoUnit.MILLIS.between(DateTimeSimulator.getCurrentDate(),
                                lastUpdate.plusMinutes(MINUTES_BETWEEN_UPDATES))/DateTimeSimulator.getTimeScale());
            step = Step.INIT;
            if(blockTime > 0)
                block(blockTime);
        }  else {
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
        float requiredHeatingPower = calculateRequiredHeatingPower(requiredTemperatureSlope);
        return new Machinery(
                prepareNextAirConditioner(requiredHeatingPower),
                prepareNextHeater(requiredHeatingPower),
                prepareNextVentilator()
        );
    }

    private float getRequiredTemperatureSlope() {

        if(meetingInProgress()) {
            return (context.getNextMeeting().getTemperature()
                    - currentClimate.getTemperature()) / ChronoUnit.SECONDS.between(
                    DateTimeSimulator.getCurrentDate(),
                    DateTimeSimulator.getCurrentDate().plusMinutes(MINUTES_BETWEEN_UPDATES));
        }
        else {
            return (context.getNextMeeting().getTemperature()
                    - currentClimate.getTemperature()) / ChronoUnit.SECONDS.between(
                    DateTimeSimulator.getCurrentDate(),
                    Conversions.toLocalDateTime(context.getNextMeeting().getStartDate()));
        }
    }

    private float calculateRequiredHeatingPower(float requiredTemperatureSlope) {
        List<Float> arguments = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        roomStatuses.stream()
                .sorted(Comparator.comparing(RoomStatus::getTemperatureSlope))
                .forEachOrdered(status-> {
                    arguments.add(status.getTemperatureSlope());
                    values.add(status.getHeatingPower());
                });
        float unboundRequiredHeatingPower = Interpolation.calculateValueAt(requiredTemperatureSlope,
                arguments, values);
        return Math.max(Math.min(unboundRequiredHeatingPower,
                currentMachinery.getHeater().getHeatingPower().getMaxValue()),
                -currentMachinery.getAirConditioner().getAirExchangedPerSecond().getMaxValue());

    }

    private AirConditioner prepareNextAirConditioner(float requiredHeatingPower) {
        if (requiredHeatingPower < -POWER_TOLERANCE) {
            return new AirConditioner(
                    new MachineParameter(
                            currentMachinery.getAirConditioner().getAirExchangedPerSecond().getMaxValue(), null),
                    new MachineParameter(
                            -requiredHeatingPower, null));
        } else if (!Helpers.almostEqual(
                0.0f,
                currentMachinery.getHeater().getHeatingPower().getCurrentValue(),
                POWER_TOLERANCE)) {
            return new AirConditioner(
                    new MachineParameter(
                            0.0f, null),
                    new MachineParameter(
                            0.0f, null));
        }
        return null;
    }

    private Ventilator prepareNextVentilator() {
        if (!Helpers.almostEqual(
                currentMachinery.getVentilator().getAirExchangedPerSecond().getCurrentValue(),
                context.getRequiredVentilation(),
                AIR_EXCHANGED_PER_SECOND_TOLERANCE)) {
            if (currentClimate.getAirQuality() < 1-AIR_QUALITY_TOLERANCE &&
                    !Helpers.almostEqual(
                            currentMachinery.getVentilator().getAirExchangedPerSecond().getCurrentValue(),
                            currentMachinery.getVentilator().getAirExchangedPerSecond().getMaxValue(),
                            AIR_EXCHANGED_PER_SECOND_TOLERANCE)) {
                return new Ventilator(
                        new MachineParameter(context.getRequiredVentilation(), null));
            }
            if (currentClimate.getAirQuality() > 1+AIR_QUALITY_TOLERANCE &&
                    !Helpers.almostEqual(
                            currentMachinery.getVentilator().getAirExchangedPerSecond().getCurrentValue(),
                            0, AIR_EXCHANGED_PER_SECOND_TOLERANCE)) {
                return new Ventilator(
                        new MachineParameter(context.getRequiredVentilation(), null));
            }
        }
        return null;
    }

    private Heater prepareNextHeater(float requiredHeatingPower) {
        if (requiredHeatingPower > POWER_TOLERANCE) {
            return new Heater(new MachineParameter(
                    requiredHeatingPower, null
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
        return Conversions.toLocalDateTime(
                context.getNextMeeting().getStartDate())
                .isBefore(DateTimeSimulator.getCurrentDate())
                && Conversions.toLocalDateTime(
                context.getNextMeeting().getEndDate())
                .isAfter(DateTimeSimulator.getCurrentDate());
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