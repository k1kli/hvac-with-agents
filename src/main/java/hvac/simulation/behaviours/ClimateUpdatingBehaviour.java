package hvac.simulation.behaviours;

import com.sun.tools.javac.util.Pair;
import hvac.simulation.SimulationContext;
import hvac.simulation.rooms.Room;
import hvac.simulation.rooms.RoomClimate;
import hvac.simulation.rooms.RoomWall;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.Hashtable;

public class ClimateUpdatingBehaviour extends TickerBehaviour {
    private SimulationContext context;
    private float timeScale;

    public ClimateUpdatingBehaviour(Agent agent,
                                    int updateTickTime,
                                    SimulationContext context, float timeScale) {
        super(agent, updateTickTime);
        this.context = context;
        this.timeScale = timeScale;
    }
    @Override
    protected void onTick() {
        Hashtable<Room, RoomClimate> newClimates = new Hashtable<>();
        for(Room r : context.getRoomMap().getRooms()) {
            RoomClimate oldClimate = context.getClimates().get(r);
            RoomClimate newClimate = new RoomClimate(
                    oldClimate.getHeater(),
                    oldClimate.getAirConditioner(),
                    oldClimate.getVentilator());

            newClimate.setPeopleInRoom(oldClimate.getPeopleInRoom());
            newClimate.setTemperature(calculateTemperatureFor(r, oldClimate));
            newClimate.setAbsoluteHumidity(calculateAbsoluteHumidityFor(r, oldClimate));
            newClimate.setRelativeHumidity(calculateRelativeHumidityFor(newClimate));
            newClimate.setAirQuality(calculateAirQualityFor(r, oldClimate));
            newClimates.put(r, newClimate);
        }
        for(Room r : context.getRoomMap().getRooms()) {
            context.getClimates().put(r, newClimates.get(r));
        }
    }


    private float getDeltaTime() { return getPeriod() * 0.001f * timeScale; }

    private float calculateTemperatureFor(Room r, RoomClimate oldClimate) {
        float roomHeatCapacity = calculateRoomHeatCapacityFor(r, oldClimate);

        float deltaQ = calculateHeatingACHeatTransferFor(oldClimate)
                + calculateBetweenRoomsHeatTransferFor(r, oldClimate);

        float TPrev = oldClimate.getTemperature();
        float beforeVentilationDeltaTemp = deltaQ /roomHeatCapacity;
        float beforeVentilationTemp = TPrev + beforeVentilationDeltaTemp;

        return calculateTemperatureWithVentilationFor(r, oldClimate, beforeVentilationTemp);
    }

    private float calculateRoomHeatCapacityFor(Room r, RoomClimate oldClimate) {
        float V = r.getVolume();
        float p = context.getOutsideClimate().getPressure();
        float R = 287.058f;
        float TPrev = oldClimate.getTemperature();
        float airMassInGrams = V * p / (R*TPrev) * 1000.0f;
        return 1.012f * airMassInGrams;
    }

    private float calculateHeatingACHeatTransferFor(RoomClimate oldClimate) {
        float heatingPower = oldClimate.getHeater().getHeatingPower();
        float coolingPower = oldClimate.getAirConditioner().getCoolingPower();
        return (heatingPower - coolingPower)*getDeltaTime();
    }

    private float calculateBetweenRoomsHeatTransferFor(Room r, RoomClimate oldClimate) {
        float res = 0;
        float myTemperature = oldClimate.getTemperature();
        for(Pair<RoomWall, Room> neighborLink : context.getRoomMap().getNeighbors(r)) {
            float neighborTemperature = context.getClimates().get(neighborLink.snd).getTemperature();
            float deltaT = neighborTemperature - myTemperature;
            float QPerSecondNeighbor = neighborLink.fst.getArea()
                    *neighborLink.fst.getHeatTransferCoefficient()
                    *deltaT;
            res += QPerSecondNeighbor;
        }
        return res *getDeltaTime();
    }

    private float calculateTemperatureWithVentilationFor(Room r, RoomClimate oldClimate, float beforeVentilationTemp) {
        float airExchangedPerSecond = oldClimate.getVentilator().getExchangedAirVolumePerSecond();
        float airExchanged = Math.min(airExchangedPerSecond*getDeltaTime(), r.getVolume());
        float outsideTemperature = context.getOutsideClimate().getTemperature();
        return (beforeVentilationTemp * (r.getVolume() - airExchanged)
                + outsideTemperature * airExchanged) / r.getVolume();
    }

    private float calculateAbsoluteHumidityFor(Room r, RoomClimate oldClimate) {
        float withAC = calculateHumidityWithACFor(r, oldClimate);
        return calculateHumidityWithVentilationFor(r, oldClimate, withAC);
    }


    private float calculateHumidityWithACFor(Room r, RoomClimate oldClimate) {
        float airExchangedPerSecond = oldClimate.getAirConditioner().getExchangedAirVolumePerSecond();
        float waterRemoved = oldClimate.getAirConditioner().getWaterRemoved();
        float airExchanged = Math.min(airExchangedPerSecond*getDeltaTime(), r.getVolume());
        float oldHumidity = oldClimate.getAbsoluteHumidity();
        return oldHumidity * (r.getVolume() - waterRemoved * airExchanged) / r.getVolume();
    }

    private float calculateHumidityWithVentilationFor(Room r, RoomClimate oldClimate, float humidityWithAC) {
        float airExchangedPerSecond = oldClimate.getVentilator().getExchangedAirVolumePerSecond();
        float outsideHumidity = context.getOutsideClimate().getAbsoluteHumidity();
        float airExchanged = Math.min(airExchangedPerSecond*getDeltaTime(), r.getVolume());
        return (humidityWithAC * (r.getVolume() - airExchanged) + outsideHumidity * airExchanged)
                / r.getVolume();
    }

    private float calculateRelativeHumidityFor(RoomClimate newClimate) {
        float outsidePressure = context.getOutsideClimate().getPressure();
        float tempCelsius = newClimate.getTemperature() - 273;
        float outsidePressureHectoPascals = outsidePressure*0.01f;
        double pressureFunctionValue = 1.0016
                + 0.00000315 * outsidePressureHectoPascals
                - 0.074 / outsidePressureHectoPascals;
        float saturationWaterVapour = (float)(pressureFunctionValue * 6.112
                * Math.exp(17.62*tempCelsius/(243.12+tempCelsius)));
        float currentWaterVapour = newClimate.getAbsoluteHumidity() * 461.5f * newClimate.getTemperature();
        return currentWaterVapour/saturationWaterVapour * 0.01f;
    }

    private float calculateAirQualityFor(Room r, RoomClimate oldClimate) {
        int peopleInRoom = oldClimate.getPeopleInRoom();
        float roomArea = r.getArea();
        float optimalVentilation = 2.5f * peopleInRoom + 0.3f * roomArea;
        optimalVentilation /= 1000f; //conversion to m^3/s from L/s
        float actualVentilation = oldClimate.getVentilator().getExchangedAirVolumePerSecond();
        return Math.min(1f, actualVentilation / optimalVentilation);
    }

}
