package hvac.roomupkeeper;

import hvac.ontologies.machinery.MachineryOntology;
import hvac.ontologies.meeting.MeetingOntology;
import hvac.ontologies.roomclimate.RoomClimateOntology;
import hvac.ontologies.weather.WeatherOntology;
import hvac.roomupkeeper.behaviours.ClimateUpkeepingBehaviour;
import hvac.roomupkeeper.behaviours.ConditionsReceivingBehaviour;
import hvac.time.DateTimeSimulator;
import hvac.util.df.DfHelpers;
import hvac.util.df.FindingBehaviour;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.domain.FIPANames;

import static hvac.util.Helpers.initTimeFromArgs;

public class RoomUpkeeperAgent extends Agent {
    @Override
    protected void setup() {
        if(!initTimeFromArgs(this, this::usage)) return;

        getContentManager().registerLanguage(new SLCodec(),
                FIPANames.ContentLanguage.FIPA_SL0);
        getContentManager().registerOntology(RoomClimateOntology.getInstance());
        getContentManager().registerOntology(MachineryOntology.getInstance());
        getContentManager().registerOntology(WeatherOntology.getInstance());
        getContentManager().registerOntology(MeetingOntology.getInstance());

        RoomUpkeeperContext context = getContext();
        if(context == null) {
            doDelete();
            return;
        }
        if(!DfHelpers.tryRegisterInDfWithServiceName(this, "upkeeper-"+context.getMyRoomId()))
            return;
        addBehaviour(new FindingBehaviour(this, "simulation",
                simulationDescriptor->{
            context.setSimulationAgent(simulationDescriptor.getName());
            addBehaviour(new FindingBehaviour(this, "weather-forecaster",
                    forecasterDescriptor->{
                context.setWeatherForecaster(forecasterDescriptor.getName());
                context.getLogger().setAgentName("room upkeeper (" + context.getMyRoomId() + ")");
                addBehaviour(new ClimateUpkeepingBehaviour(this, context));
                addBehaviour(new ConditionsReceivingBehaviour(this, context));
            }));
        }));
    }

    private RoomUpkeeperContext getContext() {
        if(getArguments().length != 4) {
            usage("incorrect number of arguments, required:4, provided: " + getArguments().length);
            return null;
        }
        int myRoomId;
        try {
            myRoomId = Integer.parseInt(getArguments()[2].toString());
        } catch (NumberFormatException e) {
            usage("roomId is not valid");
            return null;
        }
        if(myRoomId < 0) {
            usage("roomId is not valid");
        }
        float myRoomArea;
        try {
            myRoomArea = Float.parseFloat(getArguments()[3].toString());
        } catch (NumberFormatException e) {
            usage("room area is not valid");
            return null;
        }
        if(myRoomArea <= 0.0f) {
            usage("room area is not valid");
            return null;
        }
        return new RoomUpkeeperContext(myRoomId, myRoomArea);
    }

    private void usage(String err) {
        System.err.println("-------- Room upkeeper agent usage --------------");
        System.err.println("roomUpkeeper:hvac.roomUpkeeper.RoomUpkeeperAgent(timeScale, start_date, roomId, roomArea)");
        System.err.println("timescale - floating point value indicating speed of passing time");
        System.err.println("Date from which to start simulating \"yyyy-MM-dd HH:mm:ss\"");
        System.err.println("id of a room that will be upkept by this agent");
        System.err.println("area in m^2 of a room that will be upkept by this agent");
        System.err.println("err:" + err);
    }
}
