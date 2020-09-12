package hvac.ontologies.presence;

import jade.content.AgentAction;

@SuppressWarnings("unused")
public class RequestAddPresence implements AgentAction {
    private Presence presence;

    public RequestAddPresence() {
    }

    public RequestAddPresence(Presence presence) {
        this.presence = presence;
    }

    public Presence getPresence() {
        return presence;
    }

    public void setPresence(Presence presence) {
        this.presence = presence;
    }
}
