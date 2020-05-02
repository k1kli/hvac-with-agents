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
            RoomClimate newClimate = new RoomClimate();
            RoomClimate oldClimate = context.getClimates().get(r);
            newClimate.setVentilation(oldClimate.getVentilation());
            newClimate.setPeopleInRoom(oldClimate.getPeopleInRoom());
            newClimate.setHeaterPower(oldClimate.getHeaterPower());
            newClimate.setAcPower(oldClimate.getAcPower());
            newClimate.setTemperature(calculateTemperatureFor(r, oldClimate));
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
        return (oldClimate.getHeaterPower() - oldClimate.getAcPower())*getDeltaTime();
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
        float ventilationAirVolume = Math.min(oldClimate.getVentilation()*getDeltaTime(), r.getVolume());
        float outsideTemperature = context.getOutsideClimate().getTemperature();
        return (beforeVentilationTemp * (r.getVolume() - ventilationAirVolume)
                + outsideTemperature * ventilationAirVolume) / r.getVolume();
    }

}
