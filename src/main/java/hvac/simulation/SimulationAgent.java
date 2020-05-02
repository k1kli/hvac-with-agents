package hvac.simulation;

import hvac.simulation.behaviours.ClimateUpdatingBehaviour;
import hvac.simulation.machinery.AirConditioner;
import hvac.simulation.machinery.Heater;
import hvac.simulation.machinery.Ventilator;
import hvac.simulation.rooms.Room;
import hvac.simulation.rooms.RoomClimate;
import hvac.simulation.rooms.RoomWall;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class SimulationAgent extends Agent {
    SimulationContext simulationContext = new SimulationContext();

    @Override
    protected void setup() {
        loadMap();
        setDefaultClimate();
        addBehaviour(new ClimateUpdatingBehaviour(
                this, 1000, simulationContext, 10f));
        addBehaviour(new TickerBehaviour(this, 1010) {
            @Override
            protected void onTick() {
                int i = 0;
                for (Room r : simulationContext.getRoomMap().getRooms()) {
                    System.out.println("Humidity in room " + (i++) + " is "
                            + simulationContext.getClimates().get(r).getRelativeHumidity());
                }
            }
        });
    }


    private void loadMap() {
        Room r1 = new Room(200, 50);
        Room r2 = new Room(250, 70);
        Room r3 = new Room(150, 35);
        Room r4 = new Room(300, 80);
        RoomWall r12 = new RoomWall(24, 0.4f);
        RoomWall r23 = new RoomWall(16, 0.2f);
        RoomWall r34 = new RoomWall(18, 0.5f);
        RoomWall r41 = new RoomWall(40, 0.1f);
        simulationContext.getRoomMap().addRoom(r1);
        simulationContext.getRoomMap().addRoom(r2);
        simulationContext.getRoomMap().addRoom(r3);
        simulationContext.getRoomMap().addRoom(r4);
        simulationContext.getRoomMap().linkRooms(r1, r2, r12);
        simulationContext.getRoomMap().linkRooms(r2, r3, r23);
        simulationContext.getRoomMap().linkRooms(r3, r4, r34);
        simulationContext.getRoomMap().linkRooms(r4, r1, r41);
    }

    private void setDefaultClimate() {
        int i = 0;
        for(Room r : simulationContext.getRoomMap().getRooms()) {
            RoomClimate climate = new RoomClimate(
                    new Heater(0f),
                    new AirConditioner(0f, 0f, 0.5f),
                    new Ventilator(0.02f));//20L/s == 0.02m^3/s
            climate.setAbsoluteHumidity(0.001f);
            climate.setPeopleInRoom(0);
            if(i++ == 0) climate.setTemperature(273f+40f);
            else climate.setTemperature(273f+20f);
            simulationContext.getClimates().put(r, climate);
        }
        simulationContext.getOutsideClimate().setAbsoluteHumidity(0.002f);
        simulationContext.getOutsideClimate().setPressure(100000f);
        simulationContext.getOutsideClimate().setTemperature(273f+35f);
    }
}
