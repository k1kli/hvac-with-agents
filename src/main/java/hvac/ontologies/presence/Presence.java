package hvac.ontologies.presence;

import hvac.util.Conversions;
import jade.content.Concept;
import jade.content.onto.annotations.SuppressSlot;
import jade.core.AID;

import java.time.LocalDateTime;
import java.util.Date;

@SuppressWarnings("unused")
public class Presence implements Concept {
    private AID person;
    private int roomId;
    private LocalDateTime since;
    private LocalDateTime until;

    public Presence(AID person, int roomId, LocalDateTime since, LocalDateTime until) {
        this.person = person;
        this.roomId = roomId;
        this.since = since;
        this.until = until;
    }

    public Presence() {
    }

    public AID getPerson() {
        return person;
    }

    public void setPerson(AID person) {
        this.person = person;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }


    @SuppressSlot
    public LocalDateTime getSinceLocal() {
        return since;
    }

    public void setSinceLocal(LocalDateTime since) {
        this.since = since;
    }

    public Date getSince() {
        return Conversions.toDate(since);
    }

    public void setSince(Date since) {
        this.since = Conversions.toLocalDateTime(since);
    }

    @SuppressSlot
    public LocalDateTime getUntilLocal() {
        return until;
    }

    public void setUntilLocal(LocalDateTime until) {
        this.until = until;
    }
    public Date getUntil() {
        return Conversions.toDate(until);
    }

    public void setUntil(Date until) {
        this.until = Conversions.toLocalDateTime(until);
    }
    public boolean isValid() {
        return since.isBefore(until);
    }
}
