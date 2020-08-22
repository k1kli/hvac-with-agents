package hvac.ontologies.machinery;

import jade.content.Concept;

public class Machinery implements Concept {
    private AirConditioner airConditioner;
    private Heater heater;
    private Ventilator ventilator;

    public Machinery(AirConditioner airConditioner, Heater heater, Ventilator ventilator) {
        this.airConditioner = airConditioner;
        this.heater = heater;
        this.ventilator = ventilator;
    }

    public Machinery() {
    }

    public AirConditioner getAirConditioner() {
        return airConditioner;
    }

    public void setAirConditioner(AirConditioner airConditioner) {
        this.airConditioner = airConditioner;
    }

    public Heater getHeater() {
        return heater;
    }

    public void setHeater(Heater heater) {
        this.heater = heater;
    }

    public Ventilator getVentilator() {
        return ventilator;
    }

    public void setVentilator(Ventilator ventilator) {
        this.ventilator = ventilator;
    }
}
