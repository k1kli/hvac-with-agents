package hvac.util;

import java.time.LocalDateTime;

public class Helpers {
    public static boolean isBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        return date.isAfter(start) && date.isBefore(end);
    }
}
