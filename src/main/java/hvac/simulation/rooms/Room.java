package hvac.simulation.rooms;

public class Room {
    private float volume;//in m3
    private float area;//in m2
    public Room(float volume, float area)
    {
        this.volume = volume;
        this.area = area;
    }

    public float getVolume() {
        return volume;
    }
    public float getArea() {
        return area;
    }

}
