package hvac.weather.parsing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import hvac.database.Connection;
import hvac.database.entities.WeatherSnapshot;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherSaver {

    private static class WeatherData {
        public WeatherEntry[] data;
        public static class WeatherEntry {
            public LocalDateTime time_local;
            public float temperature = 303;
            public float humidity = 80;
            public float pressure = 1000;
        }
    }

    private static final String weatherDataResource = "data/weatherData.json";
    //saves weather to database because it is easier to read from it than from file
    public static void main(String[] args) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                (json, type, jsonDeserializationContext) -> LocalDateTime.parse(
                        json.getAsJsonPrimitive().getAsString(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).create();
        File dataFile = getFileFromResources();
        System.out.println("Opening " + dataFile.getName() + " and establishing db connection");
        try(BufferedReader reader = new BufferedReader(new FileReader(dataFile));
            Connection connection = new Connection()) {
            System.out.println("Parsing weather data");
            WeatherData weatherData = gson.fromJson(reader, WeatherData.class);
            System.out.println("Processing weather data");
            WeatherSnapshot[] snapshots = processWeatherData(weatherData);
            System.out.println("Saving to db");
            saveSnapshots(snapshots, connection);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void saveSnapshots(WeatherSnapshot[] snapshots, Connection connection) {
        EntityManager entityManager = connection.createEntityManager();
        entityManager.getTransaction().begin();
        String hql = "delete from WeatherSnapshot";
        Query query = entityManager.createQuery(hql);
        query.executeUpdate();
        for (WeatherSnapshot snapshot: snapshots) {
            entityManager.persist(snapshot);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private static File getFileFromResources() {
        ClassLoader classLoader = WeatherSaver.class.getClassLoader();

        URL resource = classLoader.getResource(WeatherSaver.weatherDataResource);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }
    private static WeatherSnapshot[] processWeatherData(WeatherData weatherData) {
        WeatherSnapshot[] result = new WeatherSnapshot[weatherData.data.length];
        for(int i = 0; i < result.length; i++) {
            result[i] = new WeatherSnapshot();
            result[i].setPressureHPa(weatherData.data[i].pressure);
            result[i].setTemperatureKelvin(weatherData.data[i].temperature + 273);
            result[i].setDate(weatherData.data[i].time_local);
            result[i].setAbsoluteHumidity(calculateAbsoluteHumidity(weatherData.data[i]));
        }
        return result;
    }

    private static float calculateAbsoluteHumidity(WeatherData.WeatherEntry weatherEntry) {
        //https://planetcalc.com/2161/
        double pressureFunctionValue = 1.0016
                + 0.00000315 * weatherEntry.pressure
                - 0.074 / weatherEntry.pressure;
        float saturationWaterVapour = (float)(pressureFunctionValue * 6.112
                * Math.exp(17.62*weatherEntry.temperature/(243.12+weatherEntry.temperature)));
        //https://planetcalc.com/2167/
        float currentWaterVapour = saturationWaterVapour * weatherEntry.humidity;
        float result = currentWaterVapour/(461.5f*(weatherEntry.temperature+273));
        return currentWaterVapour/(461.5f*(weatherEntry.temperature+273));
    }
}
