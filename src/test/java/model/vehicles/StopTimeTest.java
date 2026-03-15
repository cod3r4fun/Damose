package model.vehicles;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

public class StopTimeTest {

    @Test
    void testConstructorAndGetters() {
        String tripId = "trip_42";
        LocalTime arrival = LocalTime.of(9, 15, 30);
        LocalTime departure = LocalTime.of(9, 16, 0);
        String stopId = "stop_5";
        int sequence = 3;

        StopTime stopTime = new StopTime(tripId, arrival, departure, stopId, sequence);

        assertEquals(tripId, stopTime.getTrip_id());
        assertEquals(arrival, stopTime.getArrival_time());
        assertEquals(departure, stopTime.getDeparture_time());
        assertEquals(stopId, stopTime.getStop_id());
        assertEquals(sequence, stopTime.getStop_sequence());
    }

    @Test
    void testNullTripIdThrowsException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new StopTime(null, LocalTime.NOON, LocalTime.NOON, "stop1", 1);
        });
        assertNotNull(thrown);
    }

    @Test
    void testNullStopIdThrowsException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new StopTime("trip1", LocalTime.NOON, LocalTime.NOON, null, 1);
        });
        assertNotNull(thrown);
    }

    @Test
    void testNullArrivalAndDepartureAllowed() {
        StopTime stopTime = new StopTime("trip1", null, null, "stop1", 1);
        assertNull(stopTime.getArrival_time());
        assertNull(stopTime.getDeparture_time());
        assertEquals("trip1", stopTime.getTrip_id());
        assertEquals("stop1", stopTime.getStop_id());
        assertEquals(1, stopTime.getStop_sequence());
    }

    @Test
    void testStopSequenceValues() {
        StopTime zeroSequence = new StopTime("trip1", LocalTime.NOON, LocalTime.NOON, "stop1", 0);
        assertEquals(0, zeroSequence.getStop_sequence());

        StopTime negativeSequence = new StopTime("trip1", LocalTime.NOON, LocalTime.NOON, "stop1", -1);
        assertEquals(-1, negativeSequence.getStop_sequence());
    }
}
