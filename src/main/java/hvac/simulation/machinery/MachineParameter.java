package hvac.simulation.machinery;

public class MachineParameter {
    private float currentValue;
    private final float maxValue;

    public MachineParameter(float currentValue, float maxValue) {
        this.currentValue = currentValue;
        this.maxValue = maxValue;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }

    public float getMaxValue() {
        return maxValue;
    }
}
