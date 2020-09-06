package hvac.ontologies.weather;

import jade.content.Predicate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

//forecast is a predicate because one can say weather will be like this in this time period - it is a true/false statement
public class Forecast implements Predicate {
    private List weatherSnapshots;


    public Forecast() {
        weatherSnapshots = new ArrayList();
    }

    public Forecast(List weatherSnapshots) {
        this.weatherSnapshots = weatherSnapshots;
    }

    public jade.util.leap.List getWeatherSnapshots() {
        return weatherSnapshots;
    }

    public void setWeatherSnapshots(List weatherSnapshots) {
        this.weatherSnapshots = weatherSnapshots;
    }
}
