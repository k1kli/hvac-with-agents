package hvac.simulation.machinery;

import java.time.LocalDateTime;

public class MachineParameter {
    private float currentValue;
    private final float maxValue;
    private final float powerConsumption; // in kW (kJ/s)
    private LocalDateTime lastChanged = null;

    public MachineParameter(float currentValue, float maxValue, float powerConsumption) {
        this.currentValue = currentValue;
        this.maxValue = maxValue;
        this.powerConsumption = powerConsumption;
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

    public float getPowerConsumption() {
        return powerConsumption;
    }

    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(LocalDateTime lastChanged) {
        this.lastChanged = lastChanged;
    }
}
