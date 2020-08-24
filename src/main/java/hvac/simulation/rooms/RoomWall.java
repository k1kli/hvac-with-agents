package hvac.simulation.rooms;

public class RoomWall {
    private final float area;//in m2
    private final float heatTransferCoefficient;// in W*m^(-2)*K^(-1)

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
