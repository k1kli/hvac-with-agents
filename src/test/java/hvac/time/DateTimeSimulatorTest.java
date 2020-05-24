package hvac.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DateTimeSimulatorTest {

    @Test
    public void getCurrentDate() throws InterruptedException {
        for(int i = 0; i < 20; i++) {
            //given
            LocalDateTime startDate = LocalDateTime.parse("2020-01-01T12:00:00");
            LocalDateTime expected = LocalDateTime.parse("2020-01-01T12:10:00");
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