package hvac.ontologies.roomclimate;

import jade.content.AgentAction;

public class InfoRequest implements AgentAction {
    private int roomId;

    public InfoRequest() {}

    public InfoRequest(int roomId) {
        this.roomId = roomId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
