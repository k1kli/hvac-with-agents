package hvac.simulation.rooms;

public class Room {
    private final int id;
    private final float volume;//in m3
    private final float area;//in m2
    public Room(int id, float volume, float area)
    {
        this.id = id;
        this.volume = volume;
        this.area = area;
    }

    public int getId() { return id; }

    public float getVolume() {
        return volume;
    }
    public float getArea() {
        return area;
    }

}
