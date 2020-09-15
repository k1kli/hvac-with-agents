package hvac.simulation.behaviours;

import hvac.simulation.SimulationContext;
import hvac.time.DateTimeSimulator;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class PerformanceReporterBehaviour extends TickerBehaviour {
    private final SimulationContext context;
    private static final int MINUTES_BETWEEN_UPDATES = 60;

    public PerformanceReporterBehaviour(Agent agent, SimulationContext context) {
        super(agent, (long) (MINUTES_BETWEEN_UPDATES * 60 * 1000 / DateTimeSimulator.getTimeScale()));
        this.context = context;
    }

    @Override
    protected void onTick() {
        context.getLogger().log("Total energy used: " + context.getTotalEnergyUsed() + " kJ");
        context.getLogger().log("Total unhappiness: " + context.getEmployeesUnhappiness());
        context.getLogger().log("Inefficiency KPI: " + context.getEmployeesUnhappiness() * context.getTotalEnergyUsed());
    }
}
