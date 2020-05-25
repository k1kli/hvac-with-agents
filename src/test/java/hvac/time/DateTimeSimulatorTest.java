package hvac.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeSimulatorTest {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @Test
    public void getCurrentDate() throws InterruptedException {
        for(int i = 0; i < 20; i++) {
            //given
            LocalDateTime startDate = LocalDateTime.parse("2020-01-01 12:00", formatter);
            LocalDateTime expected = startDate.plusMinutes(10);
            DateTimeSimulator.init(startDate, 60*1000);//one minute every milisecond
            Thread.sleep(10);

            //when
            LocalDateTime actual = DateTimeSimulator.getCurrentDate();

            assertTrue(0 >= expected.compareTo(actual),
                    "expected = " + expected + ", actual = " + actual);
            assertTrue(0 < expected.plusMinutes(20).compareTo(actual),
                    "expected = " + expected + ", actual = " + actual);
        }
    }

}