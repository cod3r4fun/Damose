// da rivedere

package model.vehicles;

import static org.junit.jupiter.api.Assertions.*;

import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.Test;

public class StopTest {

    @Test
    void testConstructorAndGetters() {
        GeoPosition pos = new GeoPosition(40.7128, -74.0060);
        Stop stop = new Stop("Central Station", "stop_001", pos);

        assertEquals("Central Station", stop.getName());
        assertEquals("stop_001", stop.getStopId());
        assertEquals(pos, stop.getPosition());
        assertTrue(stop.isActive());
    }

    @Test
    void testSuspendAndReactivate() {
        Stop stop = new Stop("Central Station", "stop_001", new GeoPosition(0, 0));
        assertTrue(stop.isActive());

        stop.suspend();
        assertFalse(stop.isActive());

        stop.reactivate();
        assertTrue(stop.isActive());
    }

    @Test
    void testConstructorThrowsOnNull() {
        GeoPosition pos = new GeoPosition(0, 0);
        assertThrows(IllegalArgumentException.class, () -> new Stop(null, "id", pos));
        assertThrows(IllegalArgumentException.class, () -> new Stop("name", null, pos));
        assertThrows(IllegalArgumentException.class, () -> new Stop("name", "id", null));
    }

    @Test
    void testToStringContainsKeyInfo() {
        GeoPosition pos = new GeoPosition(51.5074, -0.1278);
        Stop stop = new Stop("London Station", "stop_123", pos);
        String str = stop.toString();

        assertTrue(str.contains("London Station"));
        assertTrue(str.contains("stop_123"));
        assertTrue(str.contains(pos.toString()));
    }
}
