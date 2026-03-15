package model.vehicles;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class CalendarDatesTest {

    @Test
    void constructor_andGetters_workCorrectly() {
        LocalDate date = LocalDate.of(2024, 12, 25);
        CalendarDates cd = new CalendarDates("weekday_service", date, "2");

        assertEquals("weekday_service", cd.getService_id());
        assertEquals(date, cd.getDate());
        assertEquals("2", cd.getException_type());
    }

    @Test
    void constructor_throwsIfServiceIdIsNull() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new CalendarDates(null, date, "1");
        });
        assertEquals("service_id cannot be null", ex.getMessage());
    }

    @Test
    void constructor_throwsIfDateIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new CalendarDates("service", null, "1");
        });
        assertEquals("date cannot be null", ex.getMessage());
    }

    @Test
    void constructor_throwsIfExceptionTypeIsNull() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new CalendarDates("service", date, null);
        });
        assertEquals("exception_type cannot be null", ex.getMessage());
    }

    @Test
    void typicalUsage_serviceAddedAndRemoved() {
        LocalDate addDate = LocalDate.of(2024, 7, 4);
        CalendarDates added = new CalendarDates("holiday_service", addDate, "1");
        assertEquals("1", added.getException_type());

        LocalDate removeDate = LocalDate.of(2024, 12, 25);
        CalendarDates removed = new CalendarDates("holiday_service", removeDate, "2");
        assertEquals("2", removed.getException_type());
    }
}
