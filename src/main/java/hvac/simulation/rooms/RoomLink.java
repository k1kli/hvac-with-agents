package hvac.simulation.rooms;

public class RoomLink {
    private final Room neighbor;
    private final RoomWall wall;

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
