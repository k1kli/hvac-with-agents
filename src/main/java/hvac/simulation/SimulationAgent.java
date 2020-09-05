package hvac.simulation;

import hvac.coordinator.CoordinatorAgent;
import hvac.database.Connection;
import hvac.ontologies.machinery.MachineryOntology;
import hvac.ontologies.roomclimate.RoomClimateOntology;
import hvac.simulation.behaviours.ClimateInformingBehaviour;
import hvac.simulation.behaviours.ClimateUpdatingBehaviour;
import hvac.simulation.behaviours.MachineryInterfaceBehaviour;
import hvac.simulation.machinery.AirConditioner;
import hvac.simulation.machinery.Heater;
import hvac.simulation.machinery.MachineParameter;
import hvac.simulation.machinery.Ventilator;
import hvac.simulation.rooms.Room;
import hvac.simulation.rooms.RoomClimate;
import hvac.time.DateTimeSimulator;
import hvac.util.df.DfHelpers;
import hvac.weather.DatabaseForecastProvider;
import hvac.weatherforecaster.WeatherForecasterAgent;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.wrapper.AgentContainer;

import static hvac.util.Helpers.initTimeFromArgs;
import static hvac.util.Helpers.loadMap;

@SuppressWarnings("unused")
public class SimulationAgent extends Agent {
    SimulationContext simulationContext = new SimulationContext();
    Connection connection;
    @Override
    protected void setup() {
        if(!initTimeFromArgs(this, this::usage)) return;
        if(!DfHelpers.tryRegisterInDfWithServiceName(this, "simulation")) return;
        simulationContext.getLogger().setAgentName("simulation");
        AgentContainer myContainer = getContainerController();
        try {
            myContainer.createNewAgent("coordinator",
                    CoordinatorAgent.class.getCanonicalName(),
                    new Object[]{getArguments()[0], getArguments()[1]}).start();

            myContainer.createNewAgent("weather-forecaster",
                    WeatherForecasterAgent.class.getCanonicalName(),
                    new Object[]{getArguments()[0], getArguments()[1]}).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        simulationContext.getLogger().log("Weather and Coordinator agents started.");
        getContentManager().registerLanguage(new SLCodec(),
                FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(RoomClimateOntology.getInstance());
        getContentManager().registerOntology(MachineryOntology.getInstance());
        connection = new Connection();
        loadMap(simulationContext.getRoomMap());
        setDefaultClimate();
        addBehaviour(new ClimateUpdatingBehaviour(
                this, 1000, simulationContext, DateTimeSimulator.getTimeScale(),
                new DatabaseForecastProvider(connection)));
        Room r = simulationContext.getRoomMap().getRooms().iterator().next();
        addBehaviour(new ClimateInformingBehaviour(this, simulationContext));
        addBehaviour(new MachineryInterfaceBehaviour(this, simulationContext));
    }

    private void usage(String err) {
        System.err.println("-------- Simulation agent usage --------------");
        System.err.println("simulation:hvac.simulation.SimulationAgent(timeScale, start_date)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("err:" + err);
    }

    private void setDefaultClimate() {
        int i = 0;
        for(Room r : simulationContext.getRoomMap().getRooms()) {
            RoomClimate climate = new RoomClimate(
                    new Heater(new MachineParameter(0f, 10000.0f)),
                    new AirConditioner(
                            new MachineParameter(0f, 10000.0f),
                            new MachineParameter(0f, 0.5f),
                            0.5f),
                    new Ventilator(new MachineParameter(0.02f, 0.5f))//20L/s == 0.02m^3/s
            );
            climate.setAbsoluteHumidity(0.001f);
            climate.setPeopleInRoom(0);
            if(i++ == 0) climate.setTemperature(273f+15f);
            else climate.setTemperature(273f+20f);
            simulationContext.getClimates().put(r.getId(), climate);
        }
    }

    @Override
    protected void takeDown() {
        if(connection != null) connection.close();
    }
}
