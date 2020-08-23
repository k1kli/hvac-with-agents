package hvac.ontologies.machinery;

import jade.content.AgentAction;

public class ReportMachineryStatus implements AgentAction {
    private int roomId;

    public ReportMachineryStatus(int roomId) {
        this.roomId = roomId;
    }

    public ReportMachineryStatus() {
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
