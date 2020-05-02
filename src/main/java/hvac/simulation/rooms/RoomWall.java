package hvac.simulation.rooms;

public class RoomWall {
    private float area;
    private float heatTransferCoefficient;

    public RoomWall(float area, float heatTransferCoefficient) {
        this.area = area;
        this.heatTransferCoefficient = heatTransferCoefficient;
    }

    public float getArea() {
        return area;
    }

    public float getHeatTransferCoefficient() {
        return heatTransferCoefficient;
    }
}
