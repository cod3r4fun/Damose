// written by chatGPT

package controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateControllerTest {

    @BeforeEach
    void resetDateController() {
        // Ensure reference date is reset before each test
        DateController.reset();
    }

    @Test
    void testInitialReferenceDateIsToday() {
        LocalDate systemToday = LocalDate.now();
        LocalDate referenceDate = DateController.getReferenceDate();

        // Allow for minor timing differences (within the same day)
        assertEquals(systemToday, referenceDate, "Initial reference date should match system date");
    }

    @Test
    void testMoveReferenceOneDayAhead() {
        LocalDate initialDate = DateController.getReferenceDate();
        DateController.moveReferenceOneDayAhead();
        LocalDate newDate = DateController.getReferenceDate();

        assertEquals(initialDate.plusDays(1), newDate, "Reference date should be one day ahead");
    }

    @Test
    void testResetBringsBackToSystemDate() {
        DateController.moveReferenceOneDayAhead(); // Move forward first
        assertNotEquals(LocalDate.now(), DateController.getReferenceDate(), "Reference date should differ from system date before reset");

        DateController.reset(); // Now reset

        assertEquals(LocalDate.now(), DateController.getReferenceDate(), "Reset should restore reference date to system date");
    }

    @Test
    void testMultipleDayAdvances() {
        LocalDate baseDate = DateController.getReferenceDate();

        DateController.moveReferenceOneDayAhead();
        DateController.moveReferenceOneDayAhead();
        DateController.moveReferenceOneDayAhead();

        assertEquals(baseDate.plusDays(3), DateController.getReferenceDate(), "Reference date should be 3 days ahead");
    }
}
