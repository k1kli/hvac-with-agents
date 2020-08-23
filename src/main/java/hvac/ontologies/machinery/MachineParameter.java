package hvac.ontologies.machinery;

import jade.content.Concept;

public class MachineParameter implements Concept {
    private float currentValue;
    private Float maxValue;

    public MachineParameter(float currentValue, Float maxValue) {
        this.currentValue = currentValue;
        this.maxValue = maxValue;
    }

    public MachineParameter() {}

    public float getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Float maxValue) {
        this.maxValue = maxValue;
    }
}
