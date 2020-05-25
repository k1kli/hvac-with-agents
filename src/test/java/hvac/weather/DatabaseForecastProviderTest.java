package hvac.weather;

import hvac.database.Connection;
import hvac.database.entities.WeatherSnapshot;
import hvac.weather.interfaces.ForecastProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseForecastProviderTest {
    Connection connection;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @BeforeEach
    void setUp() {
        connection = new Connection("hvac-tests");
    }

    @AfterEach
    void tearDown() {
        connection.close();
    }

    @Test
    void getWeatherBetween() {
        //given
        LocalDateTime expectedDate = LocalDateTime.parse("2020-04-10 10:00", formatter);
        WeatherSnapshot[] expected = new WeatherSnapshot[] {
                new WeatherSnapshot(expectedDate,
                        250f, 1000f, 0.1f),
        };
        EntityManager entityManager =  connection.createEntityManager();
        entityManager.getTransaction().begin();
        WeatherSnapshot[] saved = new WeatherSnapshot[] {
                new WeatherSnapshot(expectedDate,
                        250f, 1000f, 0.1f),
                new WeatherSnapshot(expectedDate.plusMonths(1),
                        250f, 1000f, 0.1f),
                new WeatherSnapshot(expectedDate.minusMonths(1),
                        100f, 1000f, 0.1f),
        };
        for(WeatherSnapshot savedEl : saved)
            entityManager.persist(savedEl);
        entityManager.getTransaction().commit();
        entityManager.close();

        //when
        ForecastProvider provider = new DatabaseForecastProvider(connection);
        WeatherSnapshot[] actual = provider.getWeatherBetween(
                expectedDate.minusWeeks(1),
                expectedDate.plusWeeks(1));

        //then
        assertArrayEquals(expected, actual);
    }

    @Test
    void getWeatherAt() {
        //given
        LocalDateTime expectedTime = LocalDateTime.parse("2020-04-10 10:00", formatter);
        EntityManager entityManager =  connection.createEntityManager();
        entityManager.getTransaction().begin();
        WeatherSnapshot[] saved = new WeatherSnapshot[] {
                new WeatherSnapshot(expectedTime,
                        250f, 1000f, 0.1f),
                new WeatherSnapshot(expectedTime.plusHours(1),
                        250f, 1000f, 0.1f),
                new WeatherSnapshot(expectedTime.minusHours(1),
                        100f, 1000f, 0.1f),
        };
        WeatherSnapshot expected = saved[0];
        for(WeatherSnapshot savedEl : saved)
            entityManager.persist(savedEl);
        entityManager.getTransaction().commit();
        entityManager.close();

        //when
        ForecastProvider provider = new DatabaseForecastProvider(connection);
        //provides time from latest snapshot before given time
        WeatherSnapshot actual = provider.getWeatherAt(expectedTime.plusMinutes(30));

        //then
        assertEquals(expected, actual);
    }
}