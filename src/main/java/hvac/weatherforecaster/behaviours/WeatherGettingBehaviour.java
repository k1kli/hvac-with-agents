package hvac.weatherforecaster.behaviours;

import hvac.database.entities.WeatherSnapshot;
import hvac.time.DateTimeSimulator;
import hvac.weather.interfaces.ForecastProvider;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class WeatherGettingBehaviour extends TickerBehaviour {
    private final ForecastProvider provider;
    private final List<WeatherSnapshot> listToUpdate;

    public WeatherGettingBehaviour(Agent a, int weatherGettingSpeed,
                                   ForecastProvider provider, List<WeatherSnapshot> listToUpdate) {
        super(a, weatherGettingSpeed);
        this.provider = provider;
        this.listToUpdate = listToUpdate;
        loadWeather();
    }

    @Override
    protected void onTick() {
        loadWeather();
    }

    private void loadWeather()
    {
        if(listToUpdate.isEmpty() || listToUpdate.get(listToUpdate.size()-1).getDate()
                .isBefore(DateTimeSimulator.getCurrentDate().plusDays(8)))
        {
            listToUpdate.clear();
            listToUpdate.addAll(Arrays.asList(provider.getWeatherBetween(
                    DateTimeSimulator.getCurrentDate(),
                    DateTimeSimulator.getCurrentDate().plusDays(14))));
            listToUpdate.sort(Comparator.comparing(WeatherSnapshot::getDate));
        }
    }
}
