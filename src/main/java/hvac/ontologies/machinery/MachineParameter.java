package hvac.ontologies.machinery;

import jade.content.Concept;

public class MachineParameter implements Concept {
    private float currentValue;
    private float maxValue;

    public MachineParameter(float currentValue, float maxValue) {
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

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }
}
