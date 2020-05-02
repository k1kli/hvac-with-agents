package hvac.simulation;

import hvac.simulation.rooms.Room;
import hvac.simulation.rooms.RoomClimate;
import hvac.simulation.rooms.RoomWall;
import jade.core.Agent;

public class SimulationAgent extends Agent {
    SimulationContext simulationContext = new SimulationContext();

    @Override
    protected void setup() {
        loadMap();
        setDefaultClimate();
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
        for(Room r : simulationContext.getRoomMap().getRooms()) {
            RoomClimate climate = new RoomClimate();
            climate.setAbsoluteHumidity(0.001f);
            climate.setAcPower(0f);
            climate.setHeaterPower(0f);
            climate.setPeopleInRoom(0);
            climate.setTemperature(300f);
            climate.setVentilation(20f);
            simulationContext.getClimates().put(r, climate);
        }
    }
}
