package hvac.ontologies.machinery;

import jade.content.AgentAction;

public class UpdateMachinery implements AgentAction {
    private Machinery machinery;
    private int roomId;

    public UpdateMachinery(Machinery machinery, int roomId) {
        this.machinery = machinery;
        this.roomId = roomId;
    }

    public UpdateMachinery() {
    }

    public Machinery getMachinery() {
        return machinery;
    }

    public void setMachinery(Machinery machinery) {
        this.machinery = machinery;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
