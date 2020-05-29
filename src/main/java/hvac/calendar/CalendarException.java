package hvac.calendar;

public class CalendarException extends Exception {
    public CalendarException(Exception innerException) {
        super(innerException);
    }
    public CalendarException() {
        super();
    }
}
