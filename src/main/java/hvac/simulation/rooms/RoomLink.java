package hvac.simulation.rooms;

public class RoomLink {
    private Room neighbor;
    private RoomWall wall;

    public RoomLink(Room neighbor, RoomWall wall) {
        this.neighbor = neighbor;
        this.wall = wall;
    }

    public Room getNeighbor() {
        return neighbor;
    }

    public RoomWall getWall() {
        return wall;
    }
}
