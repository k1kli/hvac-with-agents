package hvac.ontologies.presence;

import jade.content.Predicate;

import java.util.List;

public class PresencesInfo implements Predicate {
    private List<Presence> presences;

    public PresencesInfo() {
    }

    public PresencesInfo(List<Presence> presences) {
        this.presences = presences;
    }

    public List<Presence> getPresences() {
        return presences;
    }

    public void setPresences(List<Presence> presences) {
        this.presences = presences;
    }
}
