package hvac.simulation;

import hvac.database.Connection;
import hvac.simulation.behaviours.ClimateUpdatingBehaviour;
import hvac.simulation.machinery.AirConditioner;
import hvac.simulation.machinery.Heater;
import hvac.simulation.machinery.Ventilator;
import hvac.simulation.rooms.Room;
import hvac.simulation.rooms.RoomClimate;
import hvac.simulation.rooms.RoomWall;
import hvac.time.DateTimeSimulator;
import hvac.weather.DatabaseForecastProvider;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@SuppressWarnings("unused")
public class SimulationAgent extends Agent {
    SimulationContext simulationContext = new SimulationContext();
    Connection connection;
    @Override
    protected void setup() {
        if (getArguments() == null || getArguments().length != 2) {
            usage("Wrong args num");
            doDelete();
            return;
        }
        LocalDateTime startTime;
        float timeScale;
        try {
            timeScale = Float.parseFloat(getArguments()[0].toString());
            startTime = LocalDateTime.parse(getArguments()[1].toString(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (NumberFormatException | DateTimeParseException e) {
            usage(e.getMessage());
            doDelete();
            return;
        }
        DateTimeSimulator.init(startTime, timeScale);
        connection = new Connection();
        loadMap();
        setDefaultClimate();
        addBehaviour(new ClimateUpdatingBehaviour(
                this, 1000, simulationContext, timeScale,
                new DatabaseForecastProvider(connection)));
        Room r = simulationContext.getRoomMap().getRooms().iterator().next();
        addBehaviour(new TickerBehaviour(this, 1010) {
            @Override
            protected void onTick() {
                System.out.println("Room 1 currently has:");
                RoomClimate climate = simulationContext.getClimates().get(r);
                float temp = climate.getTemperature() - 273f;
                float otemp = simulationContext.getOutsideClimate().getTemperature() - 273f;
                float rh = climate.getRelativeHumidity();
                float ah = climate.getAbsoluteHumidity();
                float pr = simulationContext.getOutsideClimate().getPressure();
                float airQuality = climate.getAirQuality() * 100f;
                System.out.println("Now is " + DateTimeSimulator.getCurrentDate());
                System.out.println("Temperature " + temp + "(" + otemp + ") degrees C");
                System.out.println("Relative humidity " + rh + "% ah=" + ah + ", pressure=" + pr);
                System.out.println("Air quality " + airQuality + "%");
            }
        });
    }

    private void usage(String err) {
        System.err.println("-------- Simulation agent usage --------------");
        System.err.println("simulation:hvac.simulation.SimulationAgent(timeScale, start_date)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("err:" + err);
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
            if(i++ == 0) climate.setTemperature(273f+15f);
            else climate.setTemperature(273f+20f);
            simulationContext.getClimates().put(r, climate);
        }
    }

    @Override
    protected void takeDown() {
        if(connection != null) connection.close();
    }
}
