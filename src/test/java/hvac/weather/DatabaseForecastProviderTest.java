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
        EntityManager entityManager =  connection.createEntityManager();
        entityManager.getTransaction().begin();
        WeatherSnapshot[] saved = new WeatherSnapshot[] {
                new WeatherSnapshot(LocalDateTime.parse("2020-04-10 10:00", formatter),
                        250f, 1000f, 0.1f),
                new WeatherSnapshot(LocalDateTime.parse("2020-03-10 10:00", formatter),
                        250f, 1000f, 0.1f),
                new WeatherSnapshot(LocalDateTime.parse("2020-04-15 10:00", formatter),
                        100f, 1000f, 0.1f),
        };
        WeatherSnapshot[] expected = new WeatherSnapshot[] {
                saved[0], saved[2]
        };
        for(WeatherSnapshot savedEl : saved)
            entityManager.persist(savedEl);
        entityManager.getTransaction().commit();
        entityManager.close();

        //when
        ForecastProvider provider = new DatabaseForecastProvider(connection);
        WeatherSnapshot[] actual = provider.getWeatherBetween(
                LocalDateTime.parse("2020-04-01 10:00", formatter),
                LocalDateTime.parse("2020-05-01 10:00", formatter));

        //then
        assertArrayEquals(expected, actual);
    }

    @Test
    void getWeatherAt() {
        //given
        EntityManager entityManager =  connection.createEntityManager();
        entityManager.getTransaction().begin();
        WeatherSnapshot[] saved = new WeatherSnapshot[] {
                new WeatherSnapshot(LocalDateTime.parse("2020-04-10 10:00", formatter),
                        250f, 1000f, 0.1f),
                new WeatherSnapshot(LocalDateTime.parse("2020-04-10 11:00", formatter),
                        250f, 1000f, 0.1f),
                new WeatherSnapshot(LocalDateTime.parse("2020-04-10 12:00", formatter),
                        100f, 1000f, 0.1f),
        };
        WeatherSnapshot expected = saved[0];
        for(WeatherSnapshot savedEl : saved)
            entityManager.persist(savedEl);
        entityManager.getTransaction().commit();
        entityManager.close();

        //when
        ForecastProvider provider = new DatabaseForecastProvider(connection);
        WeatherSnapshot actual = provider.getWeatherAt(LocalDateTime.parse("2020-04-10 10:30", formatter));

        //then
        assertEquals(expected, actual);
    }
}