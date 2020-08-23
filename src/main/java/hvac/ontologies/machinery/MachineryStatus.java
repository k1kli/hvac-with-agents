package hvac.ontologies.machinery;

import jade.content.Predicate;

public class MachineryStatus implements Predicate {
    private Machinery machinery;
    private int roomId;

    public MachineryStatus(Machinery machinery, int roomId) {
        this.machinery = machinery;
        this.roomId = roomId;
    }

    public MachineryStatus() {
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
