package hvac.ontologies.presence;

import jade.content.Predicate;
import jade.content.onto.annotations.AggregateSlot;

import java.util.ArrayList;
import java.util.List;

public class PresencesInfo implements Predicate {
    private List<Presence> presences = new ArrayList<>();

    public PresencesInfo() {
    }

    public PresencesInfo(List<Presence> presences) {
        this.presences = presences;
    }
    @AggregateSlot
    public List<Presence> getPresences() {
        return presences;
    }

    public void setPresences(List<Presence> presences) {
        this.presences = presences;
    }
}
