package hvac.time;

import java.time.LocalDateTime;

public class DateTimeSimulator {
    private static LocalDateTime startDate;
    private static float timeScale;
    private static long realStartDate;
    private static boolean initialized = false;
    public static void init(LocalDateTime startDate, float timeScale) {
        realStartDate = System.currentTimeMillis();
        DateTimeSimulator.startDate = startDate;
        DateTimeSimulator.timeScale = timeScale;
        initialized = true;
    }

    public static LocalDateTime getCurrentDate() {
        if(!initialized) throw new RuntimeException("Not initialized");
        long realCurrentDate = System.currentTimeMillis();
        double diff = (double)(realCurrentDate - realStartDate) * timeScale * 0.001f;
        return startDate.plusSeconds((long)diff);
    }
}
