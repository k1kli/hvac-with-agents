package hvac.util.df;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class DfHelpers {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean tryRegisterInDfWithServiceName(Agent agent, String serviceName) {
        DFAgentDescription description = new DFAgentDescription();
        description.setName(agent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType("hvac");
        description.addServices(sd);
        try {
            DFService.register(agent,description);
        } catch (FIPAException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
