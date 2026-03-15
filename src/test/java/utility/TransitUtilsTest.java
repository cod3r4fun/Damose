package utility;

import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TransitUtilsTest {

    private static final GeoPosition PARIS = new GeoPosition(48.8566, 2.3522);
    private static final GeoPosition LONDON = new GeoPosition(51.5074, -0.1278);
    private static final GeoPosition SAME_PARIS = new GeoPosition(48.8566, 2.3522);

    @Test
    void haversineDistanceShouldReturnZeroForSameLocation() {
        double distance = TransitUtils.haversineDistance(PARIS, SAME_PARIS);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    void haversineDistanceShouldBeSymmetric() {
        double distance1 = TransitUtils.haversineDistance(PARIS, LONDON);
        double distance2 = TransitUtils.haversineDistance(LONDON, PARIS);
        assertEquals(distance1, distance2, 0.001);
    }

    @Test
    void haversineDistanceShouldMatchExpectedRange() {
        double distance = TransitUtils.haversineDistance(PARIS, LONDON);
        assertTrue(distance > 340000 && distance < 350000, "Expected ~343 km");
    }

    @Test
    void estimateTravelTimeTramMode() {
        Duration duration = TransitUtils.estimateTravelTime(PARIS, LONDON, 0);
        double expectedTime = TransitUtils.haversineDistance(PARIS, LONDON) / 7.0;
        assertEquals((long) expectedTime, duration.getSeconds(), 1);
    }

    @Test
    void estimateTravelTimeMetroMode() {
        Duration duration = TransitUtils.estimateTravelTime(PARIS, LONDON, 1);
        double expectedTime = TransitUtils.haversineDistance(PARIS, LONDON) / 11.1;
        assertEquals((long) expectedTime, duration.getSeconds(), 1);
    }

    @Test
    void estimateTravelTimeRailMode() {
        Duration duration = TransitUtils.estimateTravelTime(PARIS, LONDON, 2);
        double expectedTime = TransitUtils.haversineDistance(PARIS, LONDON) / 16.7;
        assertEquals((long) expectedTime, duration.getSeconds(), 1);
    }

    @Test
    void estimateTravelTimeBusMode() {
        Duration duration = TransitUtils.estimateTravelTime(PARIS, LONDON, 3);
        double expectedTime = TransitUtils.haversineDistance(PARIS, LONDON) / 5.5;
        assertEquals((long) expectedTime, duration.getSeconds(), 1);
    }

    @Test
    void estimateTravelTimeWithUnknownModeDefaultsToBus() {
        Duration defaultDuration = TransitUtils.estimateTravelTime(PARIS, LONDON, 999);
        Duration busDuration = TransitUtils.estimateTravelTime(PARIS, LONDON, 3);
        assertEquals(busDuration, defaultDuration);
    }

    @Test
    void estimateTravelTimeWithNegativeModeAlsoDefaultsToBus() {
        Duration duration = TransitUtils.estimateTravelTime(PARIS, LONDON, -1);
        Duration busDuration = TransitUtils.estimateTravelTime(PARIS, LONDON, 3);
        assertEquals(busDuration, duration);
    }

    @Test
    void estimateTravelTimeShouldReturnZeroDurationForSamePoint() {
        Duration duration = TransitUtils.estimateTravelTime(PARIS, SAME_PARIS, 0);
        assertEquals(Duration.ZERO, duration);
    }

    @Test
    void haversineDistanceShouldHandleNegativeLatLong() {
        GeoPosition point1 = new GeoPosition(-34.0, -58.0); // Buenos Aires
        GeoPosition point2 = new GeoPosition(40.7128, -74.0060); // NYC
        double distance = TransitUtils.haversineDistance(point1, point2);
        assertTrue(distance > 8000000, "Expected > 8000 km");
    }
}
