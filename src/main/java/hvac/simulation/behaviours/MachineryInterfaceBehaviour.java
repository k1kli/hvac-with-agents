package hvac.simulation.behaviours;

import hvac.ontologies.machinery.*;
import hvac.simulation.SimulationContext;
import hvac.simulation.rooms.RoomClimate;
import hvac.util.Conversions;
import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MachineryInterfaceBehaviour extends CyclicBehaviour {

    private final SimulationContext context;
    private final MessageTemplate messageTemplate = MessageTemplate.and(
            MessageTemplate.MatchOntology(MachineryOntology.getInstance().getName()),
            MessageTemplate.MatchLanguage(FIPANames.ContentLanguage.FIPA_SL0)
    );

    public MachineryInterfaceBehaviour(Agent a, SimulationContext context) {
        super(a);
        this.context = context;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive(messageTemplate);
        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.REQUEST) {
                try {
                    processRequest(msg);
                } catch (Codec.CodecException | OntologyException e) {
                    replyNotUnderstood(msg);
                }
            } else {
                replyNotUnderstood(msg);
            }
        } else {
            block();
        }
    }

    private void processRequest(ACLMessage msg) throws Codec.CodecException, OntologyException {
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
        replyNotUnderstood(msg);
    }

    private void processReportMachineryStatus(ACLMessage msg, ReportMachineryStatus reportMachineryStatus)
            throws Codec.CodecException, OntologyException {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        int roomId = reportMachineryStatus.getRoomId();
        RoomClimate climate = context.getClimates().get(roomId);
        if(climate == null) { replyRefuse(msg); return; }
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
        if(climate == null) { replyRefuse(msg); return; }
        List<ParameterUpdate> parameterUpdates = getRequestedParameterUpdates(machinery, climate);
        boolean allValid = parameterUpdates.stream().allMatch(parameterUpdate ->
                parameterUpdate.newValue >= 0 && parameterUpdate.newValue <= parameterUpdate.parameterToUpdate.getMaxValue());
        if (allValid) {
            parameterUpdates.forEach(
                    parameterUpdate -> parameterUpdate.parameterToUpdate.setCurrentValue(parameterUpdate.newValue)
            );
            replyAgree(msg);
        }
        else
            replyRefuse(msg);
    }

    private List<ParameterUpdate> getRequestedParameterUpdates(Machinery machinery, RoomClimate climate) {
        List<ParameterUpdate> parameterUpdates = new ArrayList<>();
        Optional.ofNullable(machinery.getAirConditioner()).ifPresent(airConditioner -> {
            Optional.ofNullable(airConditioner.getAirExchangedPerSecond())
                    .ifPresent(aeps ->
                            parameterUpdates.add(
                                    new ParameterUpdate(
                                            aeps.getCurrentValue(),
                                            climate.getAirConditioner().getExchangedAirVolumePerSecond())
                            )
                    );
            Optional.ofNullable(airConditioner.getCoolingPower())
                    .ifPresent(cp ->
                            parameterUpdates.add(
                                    new ParameterUpdate(
                                            cp.getCurrentValue(),
                                            climate.getAirConditioner().getCoolingPower())
                            )
                    );
        });
        Optional.ofNullable(machinery.getHeater())
                .flatMap(heater -> Optional.of(heater.getHeatingPower()))
                .ifPresent(hp -> parameterUpdates.add(
                        new ParameterUpdate(
                                hp.getCurrentValue(),
                                climate.getHeater().getHeatingPower()
                        )
                ));
        Optional.ofNullable(machinery.getVentilator())
                .flatMap(ventilator -> Optional.of(ventilator.getAirExchangedPerSecond()))
                .ifPresent(aeps -> parameterUpdates.add(
                        new ParameterUpdate(
                                aeps.getCurrentValue(),
                                climate.getVentilator().getExchangedAirVolumePerSecond()
                        )
                ));
        return parameterUpdates;
    }

    private void replyNotUnderstood(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
        myAgent.send(reply);
    }

    private void replyRefuse(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.REFUSE);
        myAgent.send(reply);
    }

    private void replyAgree(ACLMessage msg) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.AGREE);
        myAgent.send(reply);
    }
}
