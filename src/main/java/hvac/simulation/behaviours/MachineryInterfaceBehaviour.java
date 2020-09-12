package hvac.simulation.behaviours;

import hvac.ontologies.machinery.*;
import hvac.simulation.SimulationContext;
import hvac.simulation.rooms.RoomClimate;
import hvac.util.Conversions;
import hvac.util.behaviours.RequestProcessingBehaviour;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static hvac.util.SimpleReplies.*;

public class MachineryInterfaceBehaviour extends RequestProcessingBehaviour {

    private final SimulationContext context;
    private final MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchOntology(MachineryOntology.getInstance().getName()),
            MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0)
    );

    public MachineryInterfaceBehaviour(Agent a, SimulationContext context) {
        super(a, context.getLogger());
        this.context = context;
    }

    @Override
    protected void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
        ContentElement ce = myAgent.getContentManager().extractContent(msg);
        if (ce instanceof Action) {
            Concept action = ((Action) ce).getAction();
            if (action instanceof UpdateMachinery) {
                UpdateMachinery updateMachinery = (UpdateMachinery) action;
                processUpdateMachinery(msg, updateMachinery);
                return;
            } else if (action instanceof ReportMachineryStatus) {
                ReportMachineryStatus reportMachineryStatus = (ReportMachineryStatus) action;
                processReportMachineryStatus(msg, reportMachineryStatus);
                return;
            }
        }
        replyNotUnderstood(myAgent, msg);
    }

    @Override
    protected MessageTemplate getTemplate() {
        return template;
    }

    private void processReportMachineryStatus(ACLMessage msg, ReportMachineryStatus reportMachineryStatus)
            throws Codec.CodecException, OntologyException {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        int roomId = reportMachineryStatus.getRoomId();
        RoomClimate climate = context.getClimates().get(roomId);
        if(climate == null) { replyRefuse(myAgent, msg); return; }
        Machinery machinery = new Machinery(
                new AirConditioner(
                        Conversions.toOntologyParameter(climate.getAirConditioner().getExchangedAirVolumePerSecond()),
                        Conversions.toOntologyParameter(climate.getAirConditioner().getCoolingPower())
                ),
                new Heater(
                        Conversions.toOntologyParameter(climate.getHeater().getHeatingPower())
                ),
                new Ventilator(
                        Conversions.toOntologyParameter(climate.getVentilator().getExchangedAirVolumePerSecond())
                )
        );
        MachineryStatus status = new MachineryStatus(machinery, roomId);
        myAgent.getContentManager().fillContent(reply, status);
        context.getLogger().log("replying to report machinery request: roomId: " + roomId);
        myAgent.send(reply);
    }

    private static class ParameterUpdate {
        public float newValue;
        public hvac.simulation.machinery.MachineParameter parameterToUpdate;

        public ParameterUpdate(float newValue, hvac.simulation.machinery.MachineParameter parameterToUpdate) {
            this.newValue = newValue;
            this.parameterToUpdate = parameterToUpdate;
        }
    }

    private void processUpdateMachinery(ACLMessage msg, UpdateMachinery updateMachinery) {
        int roomId = updateMachinery.getRoomId();
        Machinery machinery = updateMachinery.getMachinery();
        RoomClimate climate = context.getClimates().get(roomId);
        if(climate == null) { replyRefuse(myAgent, msg); return; }
        context.getLogger().log("requested parameter updates for roomId="+roomId);
        List<ParameterUpdate> parameterUpdates = getRequestedParameterUpdates(machinery, climate);
        boolean allValid = parameterUpdates.stream().allMatch(parameterUpdate ->
                parameterUpdate.newValue >= 0 && parameterUpdate.newValue <= parameterUpdate.parameterToUpdate.getMaxValue());
        if (allValid) {
            parameterUpdates.forEach(
                    parameterUpdate -> parameterUpdate.parameterToUpdate.setCurrentValue(parameterUpdate.newValue)
            );
            replyAgree(myAgent, msg);
        }
        else
            replyRefuse(myAgent, msg);
    }

    private List<ParameterUpdate> getRequestedParameterUpdates(Machinery machinery, RoomClimate climate) {
        StringJoiner joiner = new StringJoiner(",");
        List<ParameterUpdate> parameterUpdates = new ArrayList<>();
        Optional.ofNullable(machinery.getAirConditioner()).ifPresent(airConditioner -> {
            Optional.ofNullable(airConditioner.getAirExchangedPerSecond())
                    .ifPresent(aeps -> {
                        parameterUpdates.add(
                                new ParameterUpdate(
                                        aeps.getCurrentValue(),
                                        climate.getAirConditioner().getExchangedAirVolumePerSecond())
                        );
                        joiner.add("ac-aeps="+aeps.getCurrentValue());
                            }
                    );
            Optional.ofNullable(airConditioner.getCoolingPower())
                    .ifPresent(cp -> {
                                parameterUpdates.add(
                                        new ParameterUpdate(
                                                cp.getCurrentValue(),
                                                climate.getAirConditioner().getCoolingPower())
                                );
                        joiner.add("ac-cp="+cp.getCurrentValue());
                            }
                    );
        });
        Optional.ofNullable(machinery.getHeater())
                .flatMap(heater -> Optional.of(heater.getHeatingPower()))
                .ifPresent(hp -> {
                    parameterUpdates.add(
                            new ParameterUpdate(
                                    hp.getCurrentValue(),
                                    climate.getHeater().getHeatingPower()
                            )
                    );
                    joiner.add("heater-hp="+hp.getCurrentValue());
                });
        Optional.ofNullable(machinery.getVentilator())
                .flatMap(ventilator -> Optional.of(ventilator.getAirExchangedPerSecond()))
                .ifPresent(aeps -> {
                    parameterUpdates.add(
                            new ParameterUpdate(
                                    aeps.getCurrentValue(),
                                    climate.getVentilator().getExchangedAirVolumePerSecond()
                            )
                    );
                    joiner.add("ventilator-aeps="+aeps.getCurrentValue());
                });
        context.getLogger().log("requested parameter updates: " + joiner.toString());
        return parameterUpdates;
    }
}
