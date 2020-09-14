package hvac.simulation;

import hvac.calendar.CalendarException;
import hvac.calendar.CalendarWrapper;
import hvac.coordinator.CoordinatorAgent;
import hvac.database.Connection;
import hvac.ontologies.machinery.MachineryOntology;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.roomclimate.RoomClimateOntology;
import hvac.roomupkeeper.RoomUpkeeperAgent;
import hvac.simulation.behaviours.AgentlessModeHandlingBehaviour;
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
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.wrapper.AgentContainer;

import static hvac.util.Helpers.initTimeFromArgs;
import static hvac.util.Helpers.loadMap;

@SuppressWarnings("unused")
public class SimulationAgent extends Agent {
    SimulationContext simulationContext;
    @Override
    protected void setup() {
        if(!initTimeFromArgs(this, this::usage)) return;
        if(!DfHelpers.tryRegisterInDfWithServiceName(this, "simulation")) return;
        boolean agentless;
        try {
            agentless = Boolean.parseBoolean((String) getArguments()[2]);
        } catch (Exception e) {
            usage("incorrect third argument");
            return;
        }
        simulationContext = new SimulationContext(agentless, new Connection());
        simulationContext.getLogger().setAgentName("simulation");
        loadMap(simulationContext.getRoomMap());
        if(agentless) {
            initAgentlessAgents();
            if(!tryInitCalendar()) return;
        } else {
            initAgentfullAgents();
        }
        getContentManager().registerLanguage(new SLCodec(),
                FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(RoomClimateOntology.getInstance());
        getContentManager().registerOntology(MachineryOntology.getInstance());
        getContentManager().registerOntology(MeetingOntology.getInstance());
        setDefaultClimate();
        addBehaviour(new ClimateUpdatingBehaviour(
                this, 1000, simulationContext, DateTimeSimulator.getTimeScale(),
                new DatabaseForecastProvider(simulationContext.getConnection())));
        addBehaviour(new ClimateInformingBehaviour(this, simulationContext));
        addBehaviour(new MachineryInterfaceBehaviour(this, simulationContext));
        if(agentless) addBehaviour(new AgentlessModeHandlingBehaviour(this, simulationContext));
    }

    private boolean tryInitCalendar() {
        try {
            CalendarWrapper wrapper = new CalendarWrapper();
            simulationContext.setCalendar(wrapper.getCalendarService());
        } catch (CalendarException e) {
            System.err.println("failed to obtain calendar");
            e.printStackTrace();
            doDelete();
            return false;
        }
        return true;
    }

    private void initAgentlessAgents() {
        AgentContainer myContainer = getContainerController();
        try {
            for (Room room :
                    simulationContext.getRoomMap().getRooms()) {
                myContainer.createNewAgent("room-upkeeper-" + room.getId(),
                        RoomUpkeeperAgent.class.getCanonicalName(),
                        new Object[]{getArguments()[0], getArguments()[1], room.getId(), room.getArea()}).start();
                simulationContext.getUpkeepers().put(room.getId(),
                        new AID("room-upkeeper-" + room.getId() + "@" + myContainer.getPlatformName(),AID.ISGUID));
            }

            myContainer.createNewAgent("weather-forecaster",
                    WeatherForecasterAgent.class.getCanonicalName(),
                    new Object[]{getArguments()[0], getArguments()[1]}).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        simulationContext.getLogger().log("Room upkeeper and Coordinator agents started.");
    }

    void initAgentfullAgents() {
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

    }

    private void usage(String err) {
        System.err.println("-------- Simulation agent usage --------------");
        System.err.println("simulation:hvac.simulation.SimulationAgent(timeScale, start_date, isAgentless)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("start_date - Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("isAgentless - whether to start simulation in agentlessMode (see issue #27)");
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
        if(simulationContext != null && simulationContext.getConnection() != null) simulationContext.getConnection().close();
    }
}
