package hvac.weather;

import hvac.database.Connection;
import hvac.database.entities.WeatherSnapshot;
import hvac.weather.interfaces.ForecastProvider;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DatabaseForecastProvider implements ForecastProvider {

    private Connection connection;

    public DatabaseForecastProvider(Connection connection) {
        this.connection = connection;
    }

    @Override
    @SuppressWarnings("unchecked")
    public WeatherSnapshot[] getWeatherBetween(LocalDateTime d1, LocalDateTime d2) {
        if(d1.compareTo(d2) > 0) throw new RuntimeException("d1 should be before d2");
        EntityManager entityManager = connection.createEntityManager();
        entityManager.getTransaction().begin();
        Query query =entityManager.createQuery(
                "from WeatherSnapshot where WEATHER_TIMESTAMP >= :d1 and WEATHER_TIMESTAMP <= :d2");
        query.setParameter("d1", d1);
        query.setParameter("d2", d2);
        List<WeatherSnapshot> resultList = query.getResultList();
        WeatherSnapshot[] results = resultList.toArray(new WeatherSnapshot[0]);
        entityManager.getTransaction().commit();
        entityManager.close();
        return results;
    }

    @Override
    public WeatherSnapshot getWeatherAt(LocalDateTime d) {
        WeatherSnapshot[] wholeDay = getWeatherBetween(d.minusHours(3), d.plusHours(3));
        Arrays.sort(wholeDay, Comparator.comparing(WeatherSnapshot::getDate));
        if(wholeDay.length == 0 || wholeDay[0].getDate().compareTo(d) > 0)
            return null;
        for(int i = 0; i < wholeDay.length; i++) {
            if(wholeDay[i].getDate().compareTo(d) > 0) {
                return wholeDay[i-1];
            }
        }
        return wholeDay[wholeDay.length-1];
    }
}
